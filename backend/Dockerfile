# ==============================================================================
# BUILD
# ==============================================================================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn dependency:go-offline -B

# Allow opting out of tests at build time (default: run tests)
ARG SKIP_TESTS=false

COPY src ./src
RUN mvn -B -DskipTests=${SKIP_TESTS} clean package

RUN cp target/*.jar app.jar

# ==============================================================================
# RUNTIME - Otimizado para VM (12 threads + 8GB RAM)
# ==============================================================================
FROM eclipse-temurin:21-jre-jammy

RUN groupadd -r appgroup && useradd -r -g appgroup appuser
WORKDIR /app
RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app/uploads

COPY --from=build --chown=appuser:appgroup /workspace/app.jar ./app.jar

# JVM configurada para VM (pode ser sobrescrita via JAVA_OPTS no docker-compose)
ENV JAVA_OPTS="-Xmx3g -Xms2g -XX:+UseZGC -XX:MaxGCPauseMillis=200 -XX:ParallelGCThreads=8 -XX:ConcGCThreads=4 -XX:+UseStringDeduplication -Djava.security.egd=file:/dev/./urandom"

USER appuser
# EXPOSE deve usar número fixo; usar 10808 (conforme .env APP_PORT/SERVER_PORT)
EXPOSE 10808

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]