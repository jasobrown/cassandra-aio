package org.apache.cassandra.aio;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AioFileChannelTest
{
    private static final String TEXT = "this is some text";
    private static String fileName;
    private final static int FAKE_EPOLL_ID = 42;
    AioFileChannel aioFileChannel;

    @BeforeClass
    public static void before()
    {
        try
        {
            File f = File.createTempFile("aioFileChannel-test", "txt");
            fileName = f.getAbsolutePath();
            PrintWriter pw = new PrintWriter(f);
            pw.append(TEXT);
            pw.flush();
            pw.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException("could not write tmp file: " + fileName);
        }
    }

    @After
    public void after() throws IOException
    {
        if (aioFileChannel != null)
        {
            aioFileChannel.close();
            aioFileChannel = null;
        }
    }

    @Test
    public void size() throws IOException
    {
        aioFileChannel = new AioFileChannel(new File(fileName).toPath(),
                new HashSet<StandardOpenOption>(){{add(StandardOpenOption.READ);}}, FAKE_EPOLL_ID);
        Assert.assertEquals(TEXT.length(), aioFileChannel.size());
        aioFileChannel.close();
    }

    public void read() throws IOException, InterruptedException {
        aioFileChannel = new AioFileChannel(new File(fileName).toPath(), new HashSet<StandardOpenOption>(){{add(StandardOpenOption.READ);}}, FAKE_EPOLL_ID);

        ByteBuffer bb = ByteBuffer.allocateDirect(128);
        CountDownLatch latch = new CountDownLatch(1);
        TestingCompletionHandler handler = new TestingCompletionHandler();
        aioFileChannel.read(bb, 0, latch, handler);
        latch.await(5, TimeUnit.SECONDS);

        Assert.assertNull(handler.e);
        Assert.assertEquals(TEXT.length(), handler.cnt);
        Assert.assertEquals(TEXT, new String(bb.array(), 0, handler.cnt));
    }

    class TestingCompletionHandler implements CompletionHandler<Integer, CountDownLatch>
    {
        int cnt;
        Exception e;

        public void completed(Integer result, CountDownLatch attachment)
        {
            cnt = result.intValue();
            attachment.countDown();
        }

        public void failed(Throwable exc, CountDownLatch attachment)
        {
            e = new IOException(exc);
            attachment.countDown();
        }
    }
}
