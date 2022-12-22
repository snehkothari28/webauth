#!/bin/bash

while [ -d "/proc/$PID" ]
do
    kill "$(cat save_pid.txt)"
    echo "removing process"
    sleep 15
done
rm save_pid.txt