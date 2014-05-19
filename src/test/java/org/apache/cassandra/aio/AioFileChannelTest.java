package org.apache.cassandra.aio;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;

public class AioFileChannelTest
{
    private static final String TEXT = "this is some text";
    private static String fileName;

    @BeforeClass
    public static void before()
    {
        try
        {
            File f = File.createTempFile("aio-test", "txt");
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

    @Test
    public void size() throws IOException
    {
        AioFileChannel aio = new AioFileChannel(new File(fileName).toPath(), new HashSet<StandardOpenOption>(){{add(StandardOpenOption.READ);}});
        Assert.assertEquals(TEXT.length(), aio.size());
    }

}
