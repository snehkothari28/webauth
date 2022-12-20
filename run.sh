mvn clean package
rm nohup.out
nohup java -jar target/webauth-1.0.jar --spring.profiles.active=server &
echo $! > save_pid.txt