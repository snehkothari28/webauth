#!/bin/bash

mvn clean package
rm nohup.out
nohup java -jar target/webauth-1.0.jar --spring.profiles.active=server 1>./server-logs/stdout.txt 2>./server-logs/stderr.txt &
echo $! > save_pid.txt