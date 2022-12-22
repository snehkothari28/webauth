#!/bin/bash

while [ -d "/proc/$PID" ]
do
    kill "$(cat save_pid.txt)"
    echo "removing process"
    sleep 3
done
echo "process removed"
rm save_pid.txt