package org.apache.cassandra.aio;

import java.io.IOException;

public class AsyncFileException extends IOException
{
    public AsyncFileException(String s)
    {
        super(s);
    }
}
