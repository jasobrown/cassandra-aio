package org.apache.cassandra.aio;

import java.nio.ByteBuffer;

class Native
{
    /* epoll-specific calls */
    protected static native int epollCreate();
    protected static native int epollWait(int efd, int maxEvents);
    protected static native int epollDestroy(int epollFd);

    protected static native ByteBuffer createAioContext(String fileName, int epollFd, int maxIo);
    protected static native int destroyAioContext(ByteBuffer aioContext);
    protected static native void pollAioEvents(ByteBuffer aioContext, int maxIo);

    protected static native long size0(ByteBuffer aioContext);

    /**
     * @return the number of items submitted for read; should be equal to 1, else something bad happened.
     */
    protected static native int read0(ByteBuffer aioContext, Long id, ByteBuffer dst, long position, int size);

    //TODO: callback for callback() ?



//    protected native ByteBuffer newNativeBuffer(long size);
//    protected native void resetBuffer(ByteBuffer directByteBuffer, int size);
//    protected native void destroyBuffer(ByteBuffer buffer);

}
