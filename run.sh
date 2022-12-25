#!/bin/bash

./mvnw clean package
if test -f nohup.out; then
  rm nohup.out
fi
timestamp=$(date "+%Y%m%d%H%M%S")
[ ! -d server-logs ] && mkdir server-logs
[ ! -d server-logs/"$timestamp" ] && mkdir server-logs/"$timestamp"
nohup java -jar target/webauth-1.0.jar --spring.profiles.active=server >./server-logs/"$timestamp"/log.txt 2>&1 &
echo $! >save_pid.txt

echo "Running webauth server and logging at server-logs/${timestamp}"
