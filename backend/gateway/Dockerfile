
#Stage 1: Build the application
FROM gradle:8.10-jdk21-alpine AS build
WORKDIR /gateway
COPY . .
RUN gradle build --no-daemon -x test

# Copy only the Gradle configuration files first
COPY --chown=gradle:gradle build.gradle settings.gradle /gateway/
    
#Stage 2: run the app
FROM openjdk:21-jdk-slim
WORKDIR /gateway
    
# Copy compiled class files from the build stage 
COPY --from=build /gateway/build/libs/*.jar gateway.jar

EXPOSE 8080
	
# Set entrypoint for application
ENTRYPOINT exec /usr/local/openjdk-21/bin/java -jar gateway.jar
