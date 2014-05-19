cmake_minimum_required(VERSION 2.8.8)
PROJECT(cassandra-aio)
set(CMAKE_BUILD_TYPE Release)

set(SRC src/main/c)
set(FILES 
    ${SRC}/org_apache_cassandra_aio_AioFileChannel.h
    ${SRC}/org_apache_cassandra_aio_AioFileChannel.c
)
set(JAVA_HOME /usr/local/jdk/cur)
INCLUDE_DIRECTORIES(${JAVA_HOME}/include ${JAVA_HOME}/include/linux)


add_library(cassandra-aio SHARED ${FILES})
#add_library (core OBJECT ${FILES})
#add_library (static STATIC $<TARGET_OBJECTS:core>)
#add_library (shared SHARED $<TARGET_OBJECTS:core>)
