# Use a base image with JDK for building the application
FROM eclipse-temurin:21-jdk AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the Gradle build files and source code into the container
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle
COPY src ./src

# Build the Spring Boot application
RUN ./gradlew bootJar

# Use a lightweight JRE base image for running the application
FROM eclipse-temurin:21-jre

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the port your Spring Boot application runs on
EXPOSE 9090 9092

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]