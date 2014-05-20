package org.apache.cassandra.aio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Maintains a map of opened files, as well as a shared epoll context.
 */
public class AioFileChannelFactory
{
    private static final Logger logger = LoggerFactory.getLogger(AioFileChannelFactory.class);
    public static final AioFileChannelFactory INSTANCE = new AioFileChannelFactory();

    private final Map<Path, AioFileChannel> openFiles;
    final Map<Long, CompletionWrapper<CountDownLatch>> submitted;

    private final int epollFd;
    private final ExecutorService executorService;
    private final int maxEvents;
    private volatile boolean open;
    private final Lock openLock = new ReentrantLock();

    private volatile boolean runPoller;
    private final Lock pollerLock = new ReentrantLock();

    protected AioFileChannelFactory()
    {
        epollFd = Native.epollCreate();
        if (epollFd < 0)
            throw new IllegalStateException("cannot create an epoll context: " + epollFd);

        //TODO: give this a legit value
        this.maxEvents = 42;
        openFiles = new ConcurrentHashMap<>();
        submitted = new ConcurrentHashMap<>();
        executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("AsyncFilePoller"));
        open = true;
    }

    public AioFileChannel newAioFileChannel(Path path, Set<? extends OpenOption> options) throws IOException
    {
        if (!open)
            throw new IllegalStateException("Factory is closed due to file system being shut down");

        AioFileChannel afc = openFiles.get(path);
        if (afc != null)
            return afc;

        openLock.lock();
        try
        {
            afc = openFiles.get(path);
            if (afc != null)
                return afc;
            afc = new AioFileChannel(path, options, epollFd);
            openFiles.put(path, afc);

            if (!runPoller)
            {
                pollerLock.lock();
                try
                {
                    executorService.execute(new PollRunnable());
                    runPoller = true;
                }
                finally
                {
                    pollerLock.unlock();
                }
            }
            return afc;
        }
        finally
        {
            openLock.unlock();
        }
    }

    // callback form AioFileChannels when they are closed
    public void close(Path path)
    {
        if (openFiles.remove(path) == null)
        {
            logger.debug("file already removed (or not found) from open file cache: {}", path);
        }

        if (openFiles.size() == 0)
        {
            pollerLock.lock();
            try
            {
                if (openFiles.size() == 0)
                    runPoller = false;
            }
            finally
            {
                pollerLock.unlock();
            }
        }
    }

    public void close()
    {
        if (!open)
            return;
        open = false;
        executorService.shutdown();

        for (Map.Entry<Path, AioFileChannel> entry : openFiles.entrySet())
        {
            try
            {
                entry.getValue().close();
                close(entry.getKey());

                //TODO: remove entries from submitted map
            }
            catch (IOException e)
            {
                logger.info("could not close file {}", entry.getKey());
            }
        }
        Native.epollDestroy(epollFd);
    }

    public static void callback(Long eventId, int status)
    {
        CompletionWrapper<CountDownLatch> callback = INSTANCE.submitted.remove(eventId);
        if (callback == null)
        {
            logger.info("could not find id {} in the submission map", eventId);
            return;
        }

        if (status >= 0)
        {
            callback.handler.completed(status, callback.attachment);
        }
        else
        {
            callback.handler.failed(new AsyncFileException("failed!!!"), callback.attachment);
        }
    }

    class NamedThreadFactory implements ThreadFactory
    {
        protected final String id;
        protected final AtomicInteger n = new AtomicInteger(1);

        public NamedThreadFactory(String id)
        {
            this.id = id;
        }

        public Thread newThread(Runnable runnable) {
            String name = id + ":" + n.getAndIncrement();
            Thread thread = new Thread(runnable, name);
            thread.setPriority(Thread.NORM_PRIORITY);
            thread.setDaemon(true);
            return thread;
        }
    }

    private class PollRunnable implements Runnable
    {
        public void run()
        {
            while (runPoller)
            {
                Native.epollWait(epollFd, maxEvents);
            }
        }
    }

}
