# Use an official OpenJDK image as the base
FROM openjdk:17-jdk-slim

# Install Python
RUN apt-get update && \
    apt-get install -y python3 && \
    rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the built JAR file into the container (Assuming your JAR is in the target directory)
COPY target/*.jar app.jar

# Expose the backend port (replace 8080 if your Spring Boot app uses a different port)
EXPOSE 8080

# Set the PATH environment variable to include the directory of the Python executable
ENV PATH="/usr/bin:${PATH}"

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
