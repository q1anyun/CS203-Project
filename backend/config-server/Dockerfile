
#Stage 1: Build the application
FROM gradle:8.10-jdk21-alpine AS build
WORKDIR /config-server
COPY . .
RUN gradle build --no-daemon

# Copy only the Gradle configuration files first
COPY --chown=gradle:gradle build.gradle settings.gradle /config-server/
    
#Stage 2: run the app
FROM openjdk:21-jdk-slim
WORKDIR /config-server

# Copy compiled class files from the build stage 
COPY --from=build /config-server/build/libs/*.jar config-server.jar

EXPOSE 8888
	
# Set entrypoint for application
ENTRYPOINT exec /usr/local/openjdk-21/bin/java -jar config-server.jar
