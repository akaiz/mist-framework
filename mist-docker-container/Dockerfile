#FROM hypriot/rpi-java
FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD target/mist-docker-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
