# Start with a base image containing OpenJDK
FROM gradle:8.10-jdk21-alpine

# Create a directory inside the container for the application
WORKDIR /app

# Copy the built JAR file to the container
COPY build/libs/*.jar user-service.jar

# Expose the port
EXPOSE 8082

# Define the entry point to run the JAR
ENTRYPOINT ["java", "-jar", "/app/user-service.jar"]