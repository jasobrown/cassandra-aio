#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif

#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <time.h>
#include <sys/file.h>
#include <stdbool.h>
#include <sys/epoll.h>
#include <sys/eventfd.h>
#include <unistd.h>
#include <string.h>

#include "org_apache_cassandra_aio_AioFileChannel.h"


jint
JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv* env;
    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6) != JNI_OK)
    {
        return JNI_ERR;
    }
    return JNI_VERSION_1_6;
}

/* epoll-related functions */
JNIEXPORT jint JNICALL
Java_org_apache_cassandra_aio_Native_epollCreate(JNIEnv *env, jobject class)
{
    int efd = epoll_create(128);
    return efd;
}

JNIEXPORT jint JNICALL
Java_org_apache_cassandra_aio_Native_epollWait(JNIEnv *env, jobject class, jint efd, jint max)
{
    struct io_event events[max];
    int num_events = io_getevents(ioctx, 1, 512, events, NULL);
}

JNIEXPORT jint JNICALL
Java_org_apache_cassandra_aio_Native_epollClose(JNIEnv *env, jobject class, jint efd)
{
    return close(efd);
}

/* aio-related functions */
struct cassandra_aio_ctx *
convert_context(JNIEnv *env, jobject controllerAddress)
{
    return (struct cassandra_aio_ctx *)(*env)->GetDirectBufferAddress(env, controllerAddress);
}

JNIEXPORT jobject JNICALL
Java_org_apache_cassandra_aio_Native_createAioContext(JNIEnv *env, jobject class, jstring file, jint epollFd, jint max_io)
{
    struct cassandra_aio_ctx *ctx = (struct cassandra_aio_ctx *)malloc(sizeof(struct cassandra_aio_ctx));
    if (!ctx)
    {
        return NULL;
    }
    memset(ctx, 0, sizeof(struct cassandra_aio_ctx));
    ctx->epollFd = epollFd;

    const char *name = (*env)->GetStringUTFChars(env, file, NULL);
    if (name == NULL)
    {
        free(ctx);
        return NULL;
    }
    int fd = open(name, O_RDONLY | O_DIRECT, 0666);
    if (fd <= 0)
    {
        free(ctx);
        return NULL;
    }
    ctx->fd = fd;
    (*env)->ReleaseStringUTFChars(env, file, name);

    int status = io_setup(max_io, &ctx->io_context);
    if (status < 0)
    {
        close(ctx->fd);
        free(ctx);
        return NULL;
    }

    return (*env)->NewDirectByteBuffer(env, ctx, 0);
}

JNIEXPORT jlong JNICALL
Java_org_apache_cassandra_aio_Native_size0(JNIEnv *env, jobject clazz, jobject ctx_addr)
{
    struct cassandra_aio_ctx *ctx = convert_context(env, ctx_addr);
    struct stat statBuffer;
    if (fstat(ctx->fd, &statBuffer) < 0)
    {
        return -1l;
    }
    return statBuffer.st_size;
}

JNIEXPORT jint JNICALL
Java_org_apache_cassandra_aio_Native_read0(JNIEnv *env, jobject class, jobject ctx_addr, jlong id, jobject dst, jlong position, jint size)
{
    struct cassandra_aio_ctx *ctx = convert_context(env, ctx_addr);
    struct custom_iocb iocb;
    struct custom_iocb* iocbs = &iocb;

    //TODO: pass in correct buffer :)
    io_prep_pread(&iocb, ctx->fd, buf424242424, size, position);
    io_set_eventfd(NULL, <#(int)eventfd#>);
    iocb.id = id;
    io_set_eventfd(&iocb, id);
    return io_submit(ctx->io_context, 1, &iocbs);
}

JNIEXPORT jint JNICALL
Java_org_apache_cassandra_aio_Native_destroyAioContext(JNIEnv *env, jobject class, jobject ctx_addr)
{
    struct cassandra_aio_ctx *ctx = convert_context(env, ctx_addr);

    io_destroy(ctx->io_context);

    close(ctx->fd);

    //errr, do we really care about the return value???? just (try to) close everything and run
    return 1;
}



