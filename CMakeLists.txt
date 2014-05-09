cmake_minimum_required(VERSION 2.8)
PROJECT(cassandra-aio)
set(SRC src/main/c)
set(FILES 
    ${SRC}/org_apache_cassandra_io_sstable_JasonsAsyncFileChannel.h 
    ${SRC}/org_apache_cassandra_io_sstable_JasonsAsyncFileChannel.c
)
set(JAVA_HOME /usr/local/jdk/cur)
INCLUDE_DIRECTORIES(${JAVA_HOME}/include ${JAVA_HOME}/include/linux)
add_library(casandra-aio ${FILES})
