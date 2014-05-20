cmake_minimum_required(VERSION 2.8.8)
PROJECT(cassandra-aio)
set(CMAKE_BUILD_TYPE Release)

set(SRC src/main/c)
set(FILES 
    ${SRC}/org_apache_cassandra_aio_AioFileChannel.h
    ${SRC}/org_apache_cassandra_aio_AioFileChannel.c
)
set(JAVA_HOME /usr/local/jdk/cur)

### TODO: fix this with something like https://github.com/weicao/cascadb/blob/master/cmake/FindLibaio.cmake
### this path is on jeb's ubuntu box (not portable!)
link_libraries(/lib/x86_64-linux-gnu/libaio.so.1)
INCLUDE_DIRECTORIES(${JAVA_HOME}/include ${JAVA_HOME}/include/linux)

add_library(cassandra-aio SHARED ${FILES})
