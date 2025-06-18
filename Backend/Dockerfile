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

# Install Nginx
RUN apt-get update && apt-get install -y nginx

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Copy the Nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Expose only one port for Railway
EXPOSE 8080

# Start Nginx and the Spring Boot application
CMD service nginx start && java -jar app.jar