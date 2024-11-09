# Use an official OpenJDK image as the base
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the built JAR file into the container (Assuming your JAR is in target directory)
COPY target/*.jar app.jar

# Expose the backend port (replace 8080 if your Spring Boot app uses a different port)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
