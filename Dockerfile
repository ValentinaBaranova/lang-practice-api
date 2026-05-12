# Multi-stage Dockerfile for API (Kotlin + Spring Boot, Maven)
# Build stage
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Cache dependencies
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests dependency:go-offline

# Copy sources and build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests package

# Runtime stage
FROM eclipse-temurin:17-jre
ENV APP_HOME=/app \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0" \
    SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod
WORKDIR ${APP_HOME}

# Copy fat jar from build stage
COPY --from=build /workspace/target/lang-practice-api-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

# Run the application
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=$SERVER_PORT -jar app.jar"]
