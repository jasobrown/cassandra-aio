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

#include "org_apache_cassandra_aio_AioFileChannel.h"


jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv* env;
    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6) != JNI_OK)
    {
        return JNI_ERR;
    }
    return JNI_VERSION_1_6;
}

JNIEXPORT jint JNICALL Java_org_apache_cassandra_aio_AioFileChannel_epollCreate(JNIEnv *env, jobject class)
{
    int efd = epoll_create(128);
    return efd;
}

JNIEXPORT jint JNICALL Java_org_apache_cassandra_aio_AioFileChannel_open0(JNIEnv *env, jobject class, jstring file, jint max_io)
{
    const char *name = (*env)->GetStringUTFChars(env, file, NULL);
    if (name == NULL)
    {
        return -1;
    }

    int fd = open(name, O_RDONLY | O_DIRECT, 0666);
    (*env)->ReleaseStringUTFChars(env, file, name);
    return fd;
}

JNIEXPORT jlong JNICALL Java_org_apache_cassandra_aio_AioFileChannel_size0(JNIEnv *env, jobject clazz, jint fd)
{
    struct stat statBuffer;
    if (fstat(fd, &statBuffer) < 0)
    {
        return -1l;
    }
    return statBuffer.st_size;
}

JNIEXPORT jint JNICALL Java_org_apache_cassandra_aio_AioFileChannel_close0(JNIEnv *env, jobject class, jint fd)
{
    return close(fd);
}

JNIEXPORT jint JNICALL Java_org_apache_cassandra_aio_AioFileChannel_epollClose(JNIEnv *env, jobject class, jint efd)
{
    return close(efd);
}







