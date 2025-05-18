FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/PlaygroundFlux-0.0.1.jar PlaygroundFlux-0.0.1.jar

COPY config/redisson.yaml /app/config/redisson.yaml
COPY config/application.properties /app/config/application.properties

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar PlaygroundFlux-0.0.1.jar"]