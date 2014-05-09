package org.apache.cassandra.aio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import sun.nio.fs.DefaultFileSystemProvider;


/**
 * TL;DR Holy shit, the things you have to reverse engineer to get the fucking jvm to work for you.
 *
 * OK, so because the JVM doesn't support Linux's aio, we have to write our own JNI layer (fair enough).
 * However, if we want to conform to the nio2 API (so that Windows folks can take advantage of a similar
 * OS-level feature, *for free*), we have to shoe-horn in a FileSystemProvider delegator. Java7/8 wants to use SimpleAsyncFileChannel,
 * (which uses a java-land ThreadPool instead of real aio) by default, so to get it to use a differ AsynchronousFileChannelImpl
 * we have to do some arm twisting.
 *
 * Normally, I would have just subclassed something like LinuxFileSystemProvider, but Oracle doesn't ship providers
 * for other platforms than what you are on. (For example, MacOS jvms do not ship Linux or Windows Providers). The outcoming
 * being that if we did subclass LinuxFileSystemProvider, you can't compile cassandra on MacOS or Windows. Thus, we're stuck
 * with a goofy delegate pattern just to 'override' the one method we care about - newAsynchronousFileChannel().
 *
 * To wrap up: set the jvm property "java.nio.file.spi.DefaultFileSystemProvider" to the FQCN of your delegator (that is,
 * this class), and enjoy (Linux) aio to your heart's content.
 */
public class DelegateFileSystemProvider extends FileSystemProvider
{
    private static final Logger logger = LoggerFactory.getLogger(DelegateFileSystemProvider.class);
    private final FileSystemProvider delegate;

    public DelegateFileSystemProvider()
    {
        logger.info("nop ctor");
        delegate = null;
    }

    public DelegateFileSystemProvider(FileSystemProvider delegate)
    {
        logger.info("FSP ctor");
        this.delegate = delegate;
    }

    public String getScheme()
    {
        return delegate.getScheme();
    }

    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException
    {
        return delegate.newFileSystem(uri, env);
    }

    public FileSystem getFileSystem(URI uri)
    {
        try
        {
            return new DelegateFileSystem(this, delegate.getFileSystem(uri));
        }
        catch (Exception e)
        {
            logger.error("hell broke loose", e);
            return null;
        }
    }

    public Path getPath(URI uri)
    {
        return delegate.getPath(uri);
    }

    public FileSystem newFileSystem(Path path, Map<String, ?> env) throws IOException
    {
        return delegate.newFileSystem(path, env);
    }

    public InputStream newInputStream(Path path, OpenOption... options) throws IOException
    {
        return delegate.newInputStream(path, options);
    }

    public OutputStream newOutputStream(Path path, OpenOption... options) throws IOException
    {
        return delegate.newOutputStream(path, options);
    }

    public FileChannel newFileChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException
    {
        return delegate.newFileChannel(path, options, attrs);
    }

    public AsynchronousFileChannel newAsynchronousFileChannel(Path path, Set<? extends OpenOption> options, ExecutorService executor, FileAttribute<?>... attrs) throws IOException
    {
        return new AioFileChannel(path, options);
    }

    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException
    {
        return delegate.newByteChannel(path, options, attrs);
    }

    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException
    {
        return delegate.newDirectoryStream(dir, filter);
    }

    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException
    {
        delegate.createDirectory(dir, attrs);
    }

    public void createSymbolicLink(Path link, Path target, FileAttribute<?>... attrs) throws IOException
    {
        delegate.createSymbolicLink(link, target, attrs);
    }

    public void createLink(Path link, Path existing) throws IOException
    {
        delegate.createLink(link, existing);
    }

    public void delete(Path path) throws IOException
    {
        delegate.delete(path);
    }

    public boolean deleteIfExists(Path path) throws IOException
    {
        return delegate.deleteIfExists(path);
    }

    public Path readSymbolicLink(Path link) throws IOException
    {
        return delegate.readSymbolicLink(link);
    }

    public void copy(Path source, Path target, CopyOption... options) throws IOException
    {
        delegate.copy(source, target, options);
    }

    public void move(Path source, Path target, CopyOption... options) throws IOException
    {
        delegate.move(source, target, options);
    }

    public boolean isSameFile(Path path, Path path2) throws IOException
    {
        return delegate.isSameFile(path, path2);
    }

    public boolean isHidden(Path path) throws IOException
    {
        return delegate.isHidden(path);
    }

    public FileStore getFileStore(Path path) throws IOException
    {
        return delegate.getFileStore(path);
    }

    public void checkAccess(Path path, AccessMode... modes) throws IOException
    {
        delegate.checkAccess(path, modes);
    }

    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options)
    {
        return delegate.getFileAttributeView(path, type, options);
    }

    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException
    {
        return delegate.readAttributes(path, type, options);
    }

    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException
    {
        return delegate.readAttributes(path, attributes, options);
    }

    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException
    {
        delegate.setAttribute(path,attribute, value, options);
    }
}
