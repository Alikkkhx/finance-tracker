# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies (improves caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the entire source tree and package it
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create a lightweight image for running the app
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the generated JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the default Render port (usually 10000)
EXPOSE 10000

# Run the command with the dynamically injected PORT variable
# Spring Boot automatically listens to the PORT environment variable if server.port=${PORT:8080}
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=${PORT:10000}"]
