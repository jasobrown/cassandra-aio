package org.apache.cassandra.aio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
            logger.debug("native libray being loaded: {}", name);
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
    private final int maxIO;

    /**
     * Used to determine the next writing sequence.
     * This is accessed from a single thread (the Poller Thread)
     */
    private volatile long nextReadSequence = 0;

    private volatile boolean opened = false;

    /**
     * Used while inside the callbackDone and callbackError
     */
    private final Lock callbackLock = new ReentrantLock();

//    private final ReusableLatch pollerLatch = new ReusableLatch();

    private volatile Runnable poller;


    private final int fd;
    private final int epollFd;

//    private final Executor pollerExecutor;

    /**
     * AIO can't guarantee ordering over callbacks.
     * <p/>
     * We use this {@link java.util.PriorityQueue} to hold values until they are in order
     */
//    private final PriorityQueue<CallbackHolder> pendingCallbacks = new PriorityQueue<CallbackHolder>();

    public AioFileChannel(Path path, Set<? extends OpenOption> options) throws IOException
    {
        assert !options.contains(StandardOpenOption.WRITE) : "not supporting writing with async i/o";
        //TODO: give this a legit value
        this.maxIO = 42;
        maxIOSemaphore = new Semaphore(this.maxIO);
        this.fileName = path.toFile().getAbsolutePath();

        try
        {
            fd = open0(fileName, maxIO);
            if (-1 == fd)
            {
                throw new AsyncFileException("Could not open file " + fileName);
            }

            epollFd = epollCreate();
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
        return size0(fd);
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

    public <A> void read(ByteBuffer dst, long position, A attachment, CompletionHandler<Integer, ? super A> handler)
    {
        checkOpened();

    }

    public Future<Integer> read(ByteBuffer dst, long position)
    {
        checkOpened();
        return null;
    }

    public void close() throws IOException
    {
        if (!opened)
            return;
        opened = false;
//        if (poller != null)
//        {
//            stopPoller(handler);
//            // We need to make sure we won't call close until Poller is
//            // completely done, or we might get beautiful GPFs
//            pollerLatch.await();
//        }

        int ret = close0(fd);
        if (ret < 0)
            logger.warn("problem while closing the file " + fileName + ". ignoring, but error code " + ret);
        ret = closeEpoll(epollFd);
        if (ret < 0)
            logger.warn("problem while closing epoll context, but ignoring ");
    }

    /* Native methods */
    protected native int epollCreate() throws AsyncFileException;
    protected native int open0(String fileName, int maxIO);

    protected native long size0(int fd);

    /**
     *This is using org.hornetq.core.asyncio.AIOCallback
     */
    protected native void read0(Object thisObject, ByteBuffer handle, long position, long size, ByteBuffer buffer, Object aioPackageCallback) throws AsyncFileException;

    protected native int close0(int fd);
    protected native int closeEpoll(int epollFd);

//    protected native ByteBuffer newNativeBuffer(long size);
//    protected native void resetBuffer(ByteBuffer directByteBuffer, int size);
//    protected native void destroyBuffer(ByteBuffer buffer);

//    /** Poll asynchronous events from internal queues */
//    protected native void internalPollEvents(ByteBuffer handler);
//    protected native void stopPoller(ByteBuffer handler);
}
