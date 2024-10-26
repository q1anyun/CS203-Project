# Start with a base image containing OpenJDK
FROM gradle:8.10-jdk21-alpine

# Create a directory inside the container for the application
WORKDIR /app

# Copy the built JAR file to the container
COPY build/libs/*.jar config-server.jar

# Expose the port that your Config Server will use
EXPOSE 8888

# Define the entry point to run the JAR
ENTRYPOINT ["java", "-jar", "/app/config-server.jar"]