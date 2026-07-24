# Docker Implementation Guide

## Repository Analysis Summary

The producer is a Spring Boot application that is already structured well for containerization:

- **Build tool:** Gradle wrapper (`gradlew`) using Gradle `9.5.1`
- **Java version:** Java `25` toolchain configured in `build.gradle`
- **Application entry point:** `com.learnjava.library_events_producer_v2.LibraryEventsProducerV2Application`
- **HTTP port:** `8080` from `src/main/resources/application.yaml`
- **Default Kafka broker:** `localhost:9092` in local development
- **Container-friendly Kafka broker address:** `kafka:29092` when running on the same Docker network as the Kafka service from `compose.yaml`
- **Primary runtime overrides:**
  - `SPRING_KAFKA_BOOTSTRAP_SERVERS`
  - `SERVER_PORT`
  - `KAFKA_TOPIC_NAME`
  - `KAFKA_PRODUCER_PUBLISH_TIMEOUT_SECONDS`

The included `compose.yaml` already exposes Kafka with two listener styles:

- `localhost:9092` for clients running on the host machine
- `kafka:29092` for clients running inside Docker containers on the same network

## Dockerfile Design

The root `Dockerfile` uses a **multi-stage build**:

1. **Build stage**
   - Uses Java 25 to match the Gradle toolchain
   - Runs `./gradlew clean bootJar`
   - Produces an executable Spring Boot jar

2. **Runtime stage**
   - Uses a smaller Java 25 JRE image
   - Runs as a non-root user
   - Exposes port `8080`
   - Allows runtime configuration through environment variables

## Build the Image

From the repository root:

```bash
docker build -t library-events-producer-v2:latest .
```

## Run the Container

### Option 1: Connect to Kafka running on the host

Use this when Kafka is reachable on the host at port `9092`.

```bash
docker run --rm \
  --name library-events-producer-v2 \
  -p 8080:8080 \
  --add-host=host.docker.internal:host-gateway \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
  -e KAFKA_TOPIC_NAME=library-events \
  library-events-producer-v2:latest
```

### Option 2: Connect to Kafka from the existing `compose.yaml`

Start Kafka first:

```bash
docker compose up -d kafka
```

Then run the producer container on the same Docker network. The Compose-created network is usually named after the folder, for example `library-events-producer-v2_default`.

```bash
docker run --rm \
  --name library-events-producer-v2 \
  --network library-events-producer-v2_default \
  -p 8080:8080 \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092 \
  -e KAFKA_TOPIC_NAME=library-events \
  library-events-producer-v2:latest
```

## Environment Variable Examples

Spring Boot relaxed binding lets these environment variables override the YAML configuration:

```bash
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092
SERVER_PORT=8080
KAFKA_TOPIC_NAME=library-events
KAFKA_PRODUCER_PUBLISH_TIMEOUT_SECONDS=5
JAVA_OPTS=-Xms256m -Xmx512m
```

## Notes for Connecting to Kafka from Inside the Container

1. **Do not use `localhost:9092` from inside the producer container** unless Kafka is running in the same container, which it is not.
   - Inside a container, `localhost` means the container itself.

2. **Use `kafka:29092` when both containers share a Docker network.**
   - This matches the internal listener already configured in `compose.yaml`.

3. **Use `host.docker.internal:9092` when the producer container must reach Kafka on the host.**
   - On Linux, add:
     ```bash
     --add-host=host.docker.internal:host-gateway
     ```

4. **Match the broker address to how Kafka advertises itself.**
   - Host clients should use `localhost:9092`
   - Container clients should use `kafka:29092`

5. **If the producer cannot connect, check these first:**
   - The Kafka container is running
   - Both containers are on the same Docker network
   - `SPRING_KAFKA_BOOTSTRAP_SERVERS` matches the advertised listener
   - Port `8080` is published if you want to call the REST API from the host

## Recommended Verification Flow

```bash
docker compose up -d kafka
docker build -t library-events-producer-v2:latest .
docker run --rm \
  --name library-events-producer-v2 \
  --network library-events-producer-v2_default \
  -p 8080:8080 \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092 \
  library-events-producer-v2:latest
```

Then call the producer API from the host on `http://localhost:8080`.
