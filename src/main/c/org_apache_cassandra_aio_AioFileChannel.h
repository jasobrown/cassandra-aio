#include <jni.h>
#include <libaio.h>
#ifndef CASSANDRA_AIO_INCLUDE
#define CASSANDRA_AIO_INCLUDE

#ifdef __cplusplus
extern "C" {
#endif

struct cassandra_aio_ctx
{
    io_context_t io_context;
    int epollFd;
    int fd;
};

struct custom_iocb {
    struct iocb cb;
    long long id;
};


JNIEXPORT jint JNICALL Java_org_apache_cassandra_aio_Native_epollCreate(JNIEnv *env, jobject class);
JNIEXPORT jint JNICALL Java_org_apache_cassandra_aio_Native_epollWait(JNIEnv *env, jobject class, jint efd, jint max);
JNIEXPORT jint JNICALL Java_org_apache_cassandra_aio_Native_epollClose(JNIEnv *env, jobject class, jint efd);

JNIEXPORT jobject JNICALL Java_org_apache_cassandra_aio_Native_createAioContext(JNIEnv *env, jobject class, jstring file, jint epollFd, jint max_io);
JNIEXPORT jint JNICALL Java_org_apache_cassandra_aio_Native_destroyAioContext(JNIEnv *env, jobject class, jobject ctx_addr);

JNIEXPORT jlong JNICALL Java_org_apache_cassandra_aio_Native_size0(JNIEnv *env, jobject class, jobject ctx_addr);
JNIEXPORT jint JNICALL Java_org_apache_cassandra_aio_Native_read0(JNIEnv *env, jobject class, jobject ctx_addr, jlong id, jobject dst, jlong position, jint size);


#ifdef __cplusplus
}
#endif
#endif
