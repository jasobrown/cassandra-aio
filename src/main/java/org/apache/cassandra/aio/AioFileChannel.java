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

    /**
     * This definition needs to match Version.h on the native sources.
     * <p/>
     * Or else the native module won't be loaded because of version mismatches
     */
    private static final int EXPECTED_NATIVE_VERSION = 1;

    static
    {
        final String[] libraries = new String[]{ "CassandraAIO64", "CassandraAIO32" };
        boolean loaded = false;
        for (String library : libraries)
        {
            if (loadLibrary(library))
            {
                loaded = true;
                break;
            }
            else
            {
                logger.debug("native library " + library + " not found!");
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
            if (getNativeVersion() != EXPECTED_NATIVE_VERSION)
            {
                logger.debug("native library {} not at expected version", name);
                return false;
            }
            else
            {
                return true;
            }
        }
        catch (Throwable e)
        {
            logger.debug("error loading the native library " + name, e);
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

    /**
     * Pointer the the controller in c-land.
     * Warning: Beware of the pointer! It will bite you! :-)
     */
    private ByteBuffer handler;

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
            handler = init(this.getClass(), fileName, maxIO, logger);
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
        return size0(handler);
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

        if (handler != null)
        {
            closeInternal(handler);
        }
    }

    /* Native methods */
    /** A native method that does nothing, and just validate if the ELF dependencies are loaded and on the correct platform as this binary format */
    protected static native int getNativeVersion();

    protected native ByteBuffer init(Class controllerClass, String fileName, int maxIO, Object logger) throws AsyncFileException;

    protected native long size0(ByteBuffer handle);

    /**
     *This is using org.hornetq.core.asyncio.AIOCallback
     */
    protected native void read(Object thisObject, ByteBuffer handle, long position, long size, ByteBuffer buffer, Object aioPackageCallback) throws AsyncFileException;

    protected native void closeInternal(ByteBuffer handler);

//    protected native ByteBuffer newNativeBuffer(long size);
//    protected native void resetBuffer(ByteBuffer directByteBuffer, int size);
//    protected native void destroyBuffer(ByteBuffer buffer);

//    /** Poll asynchronous events from internal queues */
//    protected native void internalPollEvents(ByteBuffer handler);
//    protected native void stopPoller(ByteBuffer handler);
}
