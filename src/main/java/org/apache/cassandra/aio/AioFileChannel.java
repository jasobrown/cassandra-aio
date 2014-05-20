package org.apache.cassandra.aio;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AioFileChannel extends AsynchronousFileChannel
{
    private static final Logger logger = LoggerFactory.getLogger(AioFileChannel.class);

    static
    {
        final String[] libraries = new String[]{ "cassandra-aio" };
        boolean loaded = false;
        for (String library : libraries)
        {
            if (loadLibrary(library))
            {
                loaded = true;
                break;
            }
        }
        if (!loaded)
        {
            logger.debug("Couldn't locate LibAIO Wrapper (sadpanda)");
        }
    }

    private static boolean loadLibrary(final String name)
    {
        try
        {
            System.loadLibrary(name);
            return true;
        }
        catch (Throwable e)
        {
            logger.debug("error loading the native library " + name);
            return false;
        }
    }

    private final String fileName;
    private final Semaphore maxIOSemaphore;
    private static final AtomicLong idGen = new AtomicLong(0);
    private volatile boolean opened = false;
    private ByteBuffer aioContext;

    private final int epollFd;

    public AioFileChannel(Path path, Set<? extends OpenOption> options, int epollFd) throws IOException
    {
        assert !options.contains(StandardOpenOption.WRITE) : "not supporting writing with async i/o";
        //TODO: give this a legit value
        int maxIO = 42;
        this.epollFd = epollFd;
        maxIOSemaphore = new Semaphore(maxIO);
        this.fileName = path.toFile().getAbsolutePath();

        try
        {
            aioContext = Native.createAioContext(fileName, epollFd, maxIO);
            if (aioContext == null)
            {
                throw new AsyncFileException("Could not open file " + fileName);
            }
        }
        catch (AsyncFileException e)
        {
            throw new IOException("unable to open file for async i/o: " + fileName, e);
        }
        opened = true;
    }

    public long size() throws IOException
    {
        checkOpened();
        return Native.size0(aioContext);
    }

    public boolean isOpen()
    {
        return opened;
    }

    private void checkOpened()
    {
        if (!opened)
        {
            throw new RuntimeException("async file is not opened: " + fileName);
        }
    }

    public AsynchronousFileChannel truncate(long size) throws IOException
    {
        throw new UnsupportedOperationException("not supporting truncate yet in async i/o");
    }

    public void force(boolean metaData) throws IOException
    {
        throw new UnsupportedOperationException("not supporting writes yet in async i/o");
    }

    public <A> void lock(long position, long size, boolean shared, A attachment, CompletionHandler<FileLock, ? super A> handler)
    {
        throw new UnsupportedOperationException("not supporting locks yet in async i/o");
    }

    public Future<FileLock> lock(long position, long size, boolean shared)
    {
        throw new UnsupportedOperationException("not supporting locks yet in async i/o");
    }

    public FileLock tryLock(long position, long size, boolean shared) throws IOException
    {
        throw new UnsupportedOperationException("not supporting locks yet in async i/o");
    }

    public <A> void write(ByteBuffer src, long position, A attachment, CompletionHandler<Integer, ? super A> handler)
    {
        throw new UnsupportedOperationException("not supporting writes yet in async i/o");
    }

    public Future<Integer> write(ByteBuffer src, long position)
    {
        throw new UnsupportedOperationException("not supporting writes yet in async i/o");
    }

    public Future<Integer> read(ByteBuffer dst, long position)
    {
        throw new UnsupportedOperationException("not supporting this read() method yet");
    }

    public <A> void read(ByteBuffer dst, long position, final A attachment, final CompletionHandler<Integer, ? super A> handler)
    {
        checkOpened();
        //fabricate some identifier....
        Long id = idGen.incrementAndGet();
        maxIOSemaphore.acquireUninterruptibly();
        try
        {
            AioFileChannelFactory.INSTANCE.submitted.put(id, new CompletionWrapper<CountDownLatch>((CountDownLatch)attachment,
                                                                    (CompletionHandler<Integer, CountDownLatch>)handler));
            int cnt = Native.read0(aioContext, id, dst, position, dst.capacity());
            if (cnt != 1)
            {
                AioFileChannelFactory.INSTANCE.submitted.remove(id);
                throw new RuntimeException("could not submit read request for file " + fileName);
            }
        }
        catch (Exception e)
        {
            AioFileChannelFactory.INSTANCE.submitted.remove(id);
            handler.failed(e, attachment);
        }
        finally
        {
            maxIOSemaphore.release();
        }
    }

    public void close() throws IOException
    {
        if (!opened)
            return;
        opened = false;

        int ret = Native.destroyAioContext(aioContext);
        if (ret < 0)
            logger.warn("problem while closing the file " + fileName + ". ignoring, but error code " + ret);

        //TODO: check if we can simply null the BB here, or if any other magic needs to happen
        aioContext = null;

        AioFileChannelFactory.INSTANCE.close(new File(fileName).toPath());
    }
}
