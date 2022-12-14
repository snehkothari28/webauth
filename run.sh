#mvn clean spring-boot:start -Dspring-boot.run.arguments="--spring.profiles.active=server"
mvn clean package
java -jar target/webauth-1.0.jar --spring.profiles.active=server