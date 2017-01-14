FROM hypriot/rpi-java
VOLUME /tmp
ADD target/mist-docker-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
