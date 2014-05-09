/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_apache_cassandra_io_sstable_JasonsAsyncFileChannel */

#ifndef _Included_org_apache_cassandra_io_sstable_JasonsAsyncFileChannel
#define _Included_org_apache_cassandra_io_sstable_JasonsAsyncFileChannel
#ifdef __cplusplus
extern "C" {
#endif
#undef org_apache_cassandra_io_sstable_JasonsAsyncFileChannel_EXPECTED_NATIVE_VERSION
#define org_apache_cassandra_io_sstable_JasonsAsyncFileChannel_EXPECTED_NATIVE_VERSION 52L
/*
 * Class:     org_apache_cassandra_io_sstable_JasonsAsyncFileChannel
 * Method:    getNativeVersion
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_apache_cassandra_io_sstable_JasonsAsyncFileChannel_getNativeVersion
  (JNIEnv *, jclass);

/*
 * Class:     org_apache_cassandra_io_sstable_JasonsAsyncFileChannel
 * Method:    init
 * Signature: (Ljava/lang/Class;Ljava/lang/String;ILjava/lang/Object;)Ljava/nio/ByteBuffer;
 */
JNIEXPORT jobject JNICALL Java_org_apache_cassandra_io_sstable_JasonsAsyncFileChannel_init
  (JNIEnv *, jobject, jclass, jstring, jint, jobject);

/*
 * Class:     org_apache_cassandra_io_sstable_JasonsAsyncFileChannel
 * Method:    size0
 * Signature: (Ljava/nio/ByteBuffer;)J
 */
JNIEXPORT jlong JNICALL Java_org_apache_cassandra_io_sstable_JasonsAsyncFileChannel_size0
  (JNIEnv *, jobject, jobject);

/*
 * Class:     org_apache_cassandra_io_sstable_JasonsAsyncFileChannel
 * Method:    read
 * Signature: (Ljava/lang/Object;Ljava/nio/ByteBuffer;JJLjava/nio/ByteBuffer;Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_org_apache_cassandra_io_sstable_JasonsAsyncFileChannel_read
  (JNIEnv *, jobject, jobject, jobject, jlong, jlong, jobject, jobject);

/*
 * Class:     org_apache_cassandra_io_sstable_JasonsAsyncFileChannel
 * Method:    closeInternal
 * Signature: (Ljava/nio/ByteBuffer;)V
 */
JNIEXPORT void JNICALL Java_org_apache_cassandra_io_sstable_JasonsAsyncFileChannel_closeInternal
  (JNIEnv *, jobject, jobject);

#ifdef __cplusplus
}
#endif
#endif
