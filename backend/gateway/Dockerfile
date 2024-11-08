# Build stage: use Gradle to build the application
FROM gradle:8.10-jdk21-alpine AS build
WORKDIR /app

# Copy Gradle files and source code
COPY gradlew build.gradle settings.gradle gradle ./
COPY src ./src

# Build the JAR without running tests
RUN chmod +x gradlew && ./gradlew build --no-daemon --parallel --exclude-task test

# Package stage: use a minimal OpenJDK image
FROM openjdk:21-jdk-slim
WORKDIR /usr/app

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/*.jar gateway.jar

# Expose the port and define the entry point
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "gateway.jar"]