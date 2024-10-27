# Build stage: use a Gradle image to build the application
FROM gradle:8.10-jdk21-alpine AS build

# Set the working directory inside the container for the build stage
WORKDIR /app

# Copy the Gradle wrapper and build files to the container
COPY gradlew build.gradle settings.gradle /app/
COPY gradle /app/gradle

# Copy local Gradle cache into the container
COPY .gradle /home/gradle/.gradle

# Copy the source code to the container
COPY src /app/src

# Give execute permissions to the Gradle wrapper
RUN chmod +x /app/gradlew

# Run Gradle build to generate the JAR file (without running tests)
RUN ./gradlew build --no-daemon --parallel --exclude-task test

# Package stage: create a clean image with only the necessary runtime dependencies
FROM openjdk:21-jdk-slim

# Set environment variables for the JAR name and application directory
ENV JAR_NAME=auth-service.jar
ENV APP_HOME=/usr/app/

# Set the working directory inside the container
WORKDIR $APP_HOME

# Copy the JAR file from the build stage to the runtime stage
COPY --from=build /app/build/libs/*.jar $APP_HOME/$JAR_NAME

# Expose the port the application runs on
EXPOSE 8081

# Define the entry point to run the JAR
ENTRYPOINT ["java", "-jar", "/usr/app/auth-service.jar"]