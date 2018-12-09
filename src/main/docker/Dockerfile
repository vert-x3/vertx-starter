FROM openjdk:8-jdk-alpine

ADD *.jar /opt/app/app.jar

EXPOSE 8080

WORKDIR /opt/app
ENTRYPOINT ["java", "-Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory", "-Dvertx.cacheDirBase=/tmp", "-jar", "app.jar"]
