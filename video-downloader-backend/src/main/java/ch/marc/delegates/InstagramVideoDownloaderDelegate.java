package ch.marc.delegates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class InstagramVideoDownloaderDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        
        String url = (String) execution.getVariable("url");
        String s3BucketName = "video-download-temp";
        String s3Folder = "instagram-videos";

        String result = downloadInstagramVideo(url, s3BucketName, s3Folder);
        
        execution.setVariable("downloadResult", result);
    }

    private String downloadInstagramVideo(String url, String s3BucketName, String s3Folder) {
        String result = "";
        try {
            String pythonExecutable = "/usr/bin/python3";
            String scriptPath = "/app/scripts/video_downloader.py";            

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
