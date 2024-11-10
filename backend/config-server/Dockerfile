# Build stage: Use a Gradle image to compile the application
FROM gradle:8.10-jdk21-alpine AS build

# Set the working directory inside the container for the build stage
WORKDIR /app

# Copy Gradle files and source code
COPY gradlew build.gradle settings.gradle /app/
COPY gradle /app/gradle
COPY src /app/src

# Ensure the Gradle wrapper script is executable
RUN chmod +x gradlew

# Run the build to produce the application JAR (without running tests)
RUN ./gradlew build --no-daemon --exclude-task test

# Production stage: Use a minimal JDK runtime image
FROM openjdk:21-jdk-slim

# Copy the application JAR from the build stage
COPY --from=build /app/build/libs/*.jar /app/config-server.jar

# Expose the application port
EXPOSE 8888

# Run the application
ENTRYPOINT ["java", "-jar", "/app/config-server.jar"]