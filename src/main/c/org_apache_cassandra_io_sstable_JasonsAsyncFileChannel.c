#include <jni.h>
#include "org_apache_cassandra_io_sstable_JasonsAsyncFileChannel.h"

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env;
    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6) != JNI_OK)
    {
        return JNI_ERR;
    }
    return 0;
}

JNIEXPORT jint JNICALL Java_org_apache_cassandra_io_sstable_JasonsAsyncFileChannel_getNativeVersion
        (JNIEnv * env, jclass ref)
{
    return org_apache_cassandra_io_sstable_JasonsAsyncFileChannel_EXPECTED_NATIVE_VERSION;
}