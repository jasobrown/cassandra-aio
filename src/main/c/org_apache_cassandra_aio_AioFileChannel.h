#include <jni.h>
#ifndef _Included_org_apache_cassandra_aio_AioFileChannel
#define _Included_org_apache_cassandra_aio_AioFileChannel

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_org_apache_cassandra_aio_AioFileChannel_epollCreate(JNIEnv *env, jobject class);
JNIEXPORT jint JNICALL Java_org_apache_cassandra_aio_AioFileChannel_open0(JNIEnv *env, jobject class, jstring file, jint max_io);

JNIEXPORT jlong JNICALL Java_org_apache_cassandra_aio_AioFileChannel_size0(JNIEnv *env, jobject class, jint fd);
JNIEXPORT void JNICALL Java_org_apache_cassandra_aio_AioFileChannel_read0(JNIEnv *env, jobject class, jobject, jobject, jlong, jlong, jobject, jobject);

JNIEXPORT jint JNICALL Java_org_apache_cassandra_aio_AioFileChannel_close0(JNIEnv *env, jobject class, jint fd);
JNIEXPORT jint JNICALL Java_org_apache_cassandra_aio_AioFileChannel_epollClose(JNIEnv *env, jobject class, jint efd);


#ifdef __cplusplus
}
#endif
#endif
