package ch.marc.delegates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InstagramVideoDownloaderDelegate implements JavaDelegate {

    @Value("${video.downloader.script-path}")
    private String scriptPath;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        
        String url = (String) execution.getVariable("url");
        String urlType = (String) execution.getVariable("urlType");
        String s3BucketName = "video-download-temp";
        String s3Folder = "instagram-videos";

        execution.setVariable("bucketName", s3BucketName);
        execution.setVariable("folder", s3Folder);

        System.out.println(urlType + " URL detected");

        String result ="";

        if (urlType.contains("instagram")) {
            result = downloadInstagramVideo(url, s3BucketName, s3Folder);
        } else if (urlType.contains("youtube")) {
            result = downloadYouTubeVideo(url, s3BucketName, s3Folder);
        } else {
            result = "Invalid URL: Doesnt match Instagram or Youtube URL";
        }

        System.out.println("Download result: " + result);
        
        execution.setVariable("downloadResult", result);
    }

    private String downloadInstagramVideo(String url, String s3BucketName, String s3Folder) {
        String result = "";
        try {
            String pythonExecutable = "python3";
            
            if (scriptPath == null || scriptPath.isEmpty()) {
                System.out.println("VIDEO_DOWNLOADER_INSTAGRAM_SCRIPT_PATH environment variable not set. Using default script path.");
                scriptPath = "/app/scripts/video_downloader_instagram.py";
            }

            ProcessBuilder processBuilder = new ProcessBuilder(
                    pythonExecutable,
                    scriptPath,
                    url,
                    s3BucketName,
                    s3Folder
            );

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result += line + "\n";
            }

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                System.err.println(line);
            }

            process.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            result = "Error during download: " + e.getMessage();
        }

        return result;
    }

    private String downloadYouTubeVideo (String url, String s3BucketName, String s3Folder) {
        String result = "";
        try {
            String pythonExecutable = "python3";
            
            if (scriptPath == null || scriptPath.isEmpty()) {
                System.out.println("VIDEO_DOWNLOADER_YOUTUBE_SCRIPT_PATH environment variable not set. Using default script path.");
                scriptPath = "/app/scripts/video_downloader_youtube.py";
            }

            ProcessBuilder processBuilder = new ProcessBuilder(
                    pythonExecutable,
                    scriptPath,
                    url,
                    s3BucketName,
                    s3Folder
            );

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result += line + "\n";
            }

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                System.err.println(line);
            }

            process.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            result = "Error during download: " + e.getMessage();
        }

        return result;
    }
    
}
