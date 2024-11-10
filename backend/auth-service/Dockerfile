# Build stage: use Gradle to build the application
FROM gradle:8.10-jdk21-alpine AS build
WORKDIR /app

# Copy the Gradle wrapper, build scripts, and Gradle settings
COPY gradlew build.gradle settings.gradle ./
COPY gradle/ ./gradle/
COPY gradle/wrapper/ ./gradle/wrapper/

# Copy the source code to the container
COPY src ./src

# Ensure the Gradle wrapper script is executable
RUN chmod +x gradlew

# Run tests first to ensure they pass; fail the build if tests fail
RUN ./gradlew clean test --no-daemon --parallel

# Build the JAR after successful tests
RUN ./gradlew clean test --no-daemon --tests '*unit.*' --parallel

# Package stage: use a minimal OpenJDK image for running the application
FROM openjdk:21-jdk-slim
WORKDIR /usr/app

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/*.jar auth-service.jar

# Expose the port the application runs on
EXPOSE 8080

# Define the entry point to run the JAR
ENTRYPOINT ["java", "-jar", "auth-service.jar"]