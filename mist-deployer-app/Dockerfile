FROM hypriot/rpi-java
VOLUME /tmp
ADD target/mist-final-deployer-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8098
CMD ["java", "-jar", "app.jar","--spring.application.json='{"dockerhost":${DOCKERHOST}"]

