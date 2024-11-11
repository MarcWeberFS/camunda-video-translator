# Use an official OpenJDK image as the base
FROM openjdk:17-jdk-slim

# Install Python and necessary dependencies
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    pip3 install instaloader boto3 && \
    rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the built JAR file into the container
COPY target/*.jar app.jar

# Copy the Python script into the container
COPY src/main/resources/scripts/video_downloader.py /app/scripts/

# Set environment variable for the script path
ENV VIDEO_DOWNLOADER_SCRIPT_PATH="/app/scripts/video_downloader.py"

# Expose the backend port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
