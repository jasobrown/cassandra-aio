#!/bin/bash

###
## lame-o script to generate the jni header file and copy it over here
###

cd ./target/classes/
javah -d /tmp  org.apache.cassandra.aio.AioFileChannel
