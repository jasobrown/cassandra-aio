package org.apache.cassandra.aio;

import java.nio.channels.CompletionHandler;

class CompletionWrapper<A>
{
    final A attachment;
    final CompletionHandler<Integer, ? super A> handler;

    CompletionWrapper(A attachment, CompletionHandler<Integer, ? super A> handler)
    {
        this.attachment = attachment;
        this.handler = handler;
    }
}
