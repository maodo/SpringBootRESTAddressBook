#Official JDK8 image
#FROM java:openjdk-8-jdk
FROM java:openjdk-8-alpine
ADD SpringBootAddressBook-0.0.1-SNAPSHOT.jar /opt/SpringBootAddressBook-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENV MONGODB_DB_NAME addressbook
ENV MONGODB_DB_HOST mongo
ENV MONGODB_DB_PORT 27017
ENTRYPOINT ["java", "-jar", "/opt/SpringBootAddressBook-0.0.1-SNAPSHOT.jar"]
