#!/bin/bash

# Clean target directory to remove stale builds
rm -rf target

# Rebuild the project
./mvnw clean package -DskipTests

# Remove old logs
[ -f nohup.out ] && rm nohup.out

# Create timestamped log folder
timestamp=$(date "+%Y%m%d%H%M%S")
mkdir -p server-logs/"$timestamp"

# Run the app with profile 'server'
nohup java -Xmx512m -jar target/webauth-1.0.jar --spring.profiles.active=server > ./server-logs/"$timestamp"/log.txt 2>&1 &

# Save the process ID
echo $! > save_pid.txt

echo "Running webauth server with profile 'server' and logging at server-logs/${timestamp}"
