#!/bin/bash

./mvnw clean package
if test -f nohup.out; then
  rm nohup.out
fi
timestamp=$(date "+%Y%m%d%H%M%S")
mkdir server-logs/timestamp
nohup java -jar target/webauth-1.0.jar --spring.profiles.active=server 1>./server-logs/timestamp/stdout.txt 2>./server-logs/timestamp/stderr.txt &
echo $! > save_pid.txt