#!/bin/bash

###
## lame-o script to generate the jni header file and copy it over here
###

cd ../cassandra/build/classes/main
javah -d /usr/local/src/cassandra-aio/src/main/c  org.apache.cassandra.io.sstable.JasonsAsyncFileChannel
