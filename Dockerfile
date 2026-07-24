FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

COPY gradlew build.gradle settings.gradle ./
COPY gradle gradle

RUN chmod +x gradlew

COPY src src

RUN ./gradlew --no-daemon clean bootJar

FROM eclipse-temurin:25-jre

RUN groupadd --system spring && useradd --system --gid spring spring

WORKDIR /app

ENV SERVER_PORT=8080 \
    SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092 \
    KAFKA_TOPIC_NAME=library-events \
    KAFKA_PRODUCER_PUBLISH_TIMEOUT_SECONDS=5 \
    JAVA_OPTS=""

COPY --from=build /workspace/build/libs/*.jar /app/app.jar

EXPOSE 8080

USER spring:spring

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
