#!/bin/bash

kill "$(cat save_pid.txt)"
while [[ test -d /proc/"$PID"/ ]]
do
    echo "removing process"
    sleep 15
done
rm save_pid.txt