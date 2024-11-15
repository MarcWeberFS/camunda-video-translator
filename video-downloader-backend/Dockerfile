# Use an official OpenJDK image as the base
FROM openjdk:17-jdk-slim

# Install Python and necessary dependencies
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    pip3 install instaloader boto3 && \
    pip3 install pytube boto3 && \
    apt-get install -y ffmpeg && \
    rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Set permissions on the temp directory and its contents
RUN mkdir -p /tmp && chmod 777 /tmp

# Copy the built JAR file into the container
COPY target/*.jar app.jar

# Copy the Python script into the container
COPY src/main/resources/scripts/video_downloader.py /app/scripts/

# Decode the session file from the INSTA_SESSION environment variable
ARG INSTA_SESSION
RUN echo "$INSTA_SESSION" | base64 -d > /app/session-mxrcweber

# Set environment variable for Instaloader to use the session file
ENV INSTALOADER_SESSIONFILE="/app/session-mxrcweber"
ENV VIDEO_DOWNLOADER_SCRIPT_PATH="/app/scripts/video_downloader.py"

# Expose the backend port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
