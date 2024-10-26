#Stage 1: Build the application
FROM gradle:8.10-jdk21-alpine AS build
WORKDIR /user-service
COPY . .
RUN gradle build --no-daemon

#Stage 2: run the app
FROM openjdk:21-jdk-slim
WORKDIR /user-service

# Copy compiled class files from the build stage 
COPY --from=build /user-service/build/libs/*.jar user-service.jar

# Copy resource files from the build stage
# COPY --from=build /tournament-service/build/resources/main /tournament-service/resources

# Set entrypoint for application
ENTRYPOINT ["java", "-jar",  user-service.jar]