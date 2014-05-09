package org.apache.cassandra.io.aio;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;

public class DelegatePath implements Path
{
    final FileSystem delegator;
    private final Path target;

    public DelegatePath(FileSystem delegator, Path target)
    {
        this.delegator = delegator;
        this.target = target;
    }

    public FileSystem getFileSystem()
    {
        return delegator;
    }

    public boolean isAbsolute()
    {
        return target.isAbsolute();
    }

    public Path getRoot()
    {
        return new DelegatePath(delegator, target.getRoot());
    }

    public Path getFileName()
    {
        return new DelegatePath(delegator, target.getFileName());
    }

    public Path getParent()
    {
        return new DelegatePath(delegator, target.getParent());
    }

    public int getNameCount()
    {
        return target.getNameCount();
    }

    public Path getName(int index)
    {
        return new DelegatePath(delegator, target.getName(index));
    }

    public Path subpath(int beginIndex, int endIndex)
    {
        return new DelegatePath(delegator, target.subpath(beginIndex, endIndex));
    }

    public boolean startsWith(Path other)
    {
        return target.startsWith(other);
    }

    public boolean startsWith(String other)
    {
        return target.startsWith(other);
    }

    public boolean endsWith(Path other)
    {
        return target.endsWith(other);
    }

    public boolean endsWith(String other)
    {
        return target.endsWith(other);
    }

    public Path normalize()
    {
        return new DelegatePath(delegator, target.normalize());
    }

    public Path resolve(Path other)
    {
        return new DelegatePath(delegator, target.resolve(other));
    }

    public Path resolve(String other)
    {
        return new DelegatePath(delegator, target.resolve(other));
    }

    public Path resolveSibling(Path other)
    {
        return new DelegatePath(delegator, target.resolveSibling(other));
    }

    public Path resolveSibling(String other)
    {
        return new DelegatePath(delegator, target.resolveSibling(other));
    }

    public Path relativize(Path other)
    {
        return new DelegatePath(delegator, target.relativize(other));
    }

    public URI toUri()
    {
        return target.toUri();
    }

    public Path toAbsolutePath()
    {
        return new DelegatePath(delegator, target.toAbsolutePath());
    }

    public Path toRealPath(LinkOption... options) throws IOException
    {
        return new DelegatePath(delegator, target.toRealPath(options));
    }

    public File toFile()
    {
        return target.toFile();
    }

    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException
    {
        return target.register(watcher, events, modifiers);
    }

    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException
    {
        return target.register(watcher, events);
    }

    public Iterator<Path> iterator()
    {
        //TODO; actaully code this!
        return null;
    }

    public int compareTo(Path other)
    {
        if (other instanceof DelegatePath)
            return target.compareTo(((DelegatePath)other).target);

        return target.compareTo(other);
    }
}
