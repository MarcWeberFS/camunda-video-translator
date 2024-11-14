    package ch.marc.delegates;

    import java.io.BufferedInputStream;
    import java.io.BufferedReader;
    import java.io.BufferedWriter;
    import java.io.File;
    import java.io.FileWriter;
    import java.io.IOException;
    import java.io.InputStream;
    import java.io.InputStreamReader;
    import java.nio.charset.Charset;
    import java.nio.charset.StandardCharsets;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.Paths;
    import java.util.concurrent.TimeUnit;

    import org.camunda.bpm.engine.delegate.DelegateExecution;
    import org.camunda.bpm.engine.delegate.JavaDelegate;
    import org.springframework.stereotype.Component;

    import com.ibm.icu.text.CharsetDetector;
    import com.ibm.icu.text.CharsetMatch;

    import software.amazon.awssdk.core.sync.RequestBody;
    import software.amazon.awssdk.services.s3.S3Client;
    import software.amazon.awssdk.services.s3.model.GetObjectRequest;
    import software.amazon.awssdk.services.s3.model.PutObjectRequest;

    @Component
    public class WriteCaptionsOntoVideo implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) throws Exception {
            String videoId = (String) execution.getVariable("videoId");
            String s3BucketName = "video-download-temp";
            String s3Folder = "instagram-videos";
            String translatedText = (String) execution.getVariable("translatedText");

            String tempDir = System.getProperty("java.io.tmpdir");
            Path videoFilePath = Paths.get(tempDir, videoId + ".mp4");

            String downloadPath = downloadVideo(videoId, s3BucketName, s3Folder);

            if (downloadPath == null) {
                throw new RuntimeException("Failed to download the video from S3.");
            }

            String srtFilePath = writeSrtFile(translatedText, videoId);

            String outputFilePath = Paths.get(tempDir, videoId + "_translated.mp4").toString();
            embedSubtitles(videoFilePath.toString(), srtFilePath, outputFilePath);

            String outputS3Url = updateVideoInS3(outputFilePath, s3BucketName, s3Folder, videoId);

            deleteLocalFile(videoFilePath.toString());
            deleteLocalFile(srtFilePath);
            deleteLocalFile(outputFilePath);

            execution.setVariable("outputVideoUrl", outputS3Url);
            execution.setVariable("downloadLink", outputS3Url);
        }

        private String writeSrtFile(String translatedText, String videoId) throws IOException {
            String tempDir = System.getProperty("java.io.tmpdir");
            String srtFilePath = Paths.get(tempDir, videoId + "_translated.srt").toString();
            try (FileWriter writer = new FileWriter(srtFilePath)) {
                writer.write(translatedText);
            }
            return srtFilePath;
        }

        private void embedSubtitles(String videoFilePath, String srtFilePath, String outputFilePath) throws IOException, InterruptedException {
            Path utf8SrtPath = convertToUtf8(srtFilePath);
            String formattedSrtPath = utf8SrtPath.toString().replace("\\", "/").replace(":", "\\:");
            
            // Command should look like: ffmpeg -i "input.mp4" -vf subtitles="subtitles.srt" -c:a copy "output.mp4"
            String command = String.format("ffmpeg -i \"%s\" -vf subtitles='%s' -c:a copy \"%s\"",
                    videoFilePath, formattedSrtPath, outputFilePath);
            
            System.out.println("Executing FFmpeg command: " + command);
            
            Process process = Runtime.getRuntime().exec(command);
            Thread errorStreamReader = new Thread(() -> {
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        System.err.println(errorLine);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            
            Thread outputStreamReader = new Thread(() -> {
                try (BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String outputLine;
                    while ((outputLine = outputReader.readLine()) != null) {
                        System.out.println(outputLine);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            
            errorStreamReader.start();
            outputStreamReader.start();
            
            // 30 second timeout, occasianally the process gets stuck and never returns an exit code, even though it completed successfully
            if (!process.waitFor(30, TimeUnit.SECONDS)) {
                process.destroy();
                throw new RuntimeException("FFmpeg process timed out.");
            }
        
            if (process.exitValue() != 0) {
                throw new RuntimeException("Failed to embed subtitles with FFmpeg");
            }
            
            errorStreamReader.join();
            outputStreamReader.join();
        }
        
        
        
        
        // https://www.baeldung.com/java-string-encode-utf-8
        private Path convertToUtf8(String originalSrtPath) throws IOException {
            // SRT file is ASCII encoded and cannot interpet äöü characters, solution is to encode it in UTF-8
            Path originalPath = Paths.get(originalSrtPath);
            Path utf8Path = Paths.get(originalPath.getParent().toString(), originalPath.getFileName().toString().replace(".srt", "_utf8.srt"));

            CharsetDetector detector = new CharsetDetector();
            try (InputStream input = new BufferedInputStream(Files.newInputStream(originalPath))) { // Wrap with BufferedInputStream
                detector.setText(input);
                CharsetMatch match = detector.detect();
                Charset originalCharset = Charset.forName(match.getName());

                try (BufferedReader reader = Files.newBufferedReader(originalPath, originalCharset);
                    BufferedWriter writer = Files.newBufferedWriter(utf8Path, StandardCharsets.UTF_8)) {
                    char[] buffer = new char[1024];
                    int length;
                    while ((length = reader.read(buffer)) != -1) {
                        writer.write(buffer, 0, length);
                    }
                }
            }

            return utf8Path;
        }


        private String downloadVideo(String videoId, String s3BucketName, String s3Folder) {
            S3Client s3Client = S3Client.builder().build();
            String tempDir = System.getProperty("java.io.tmpdir"); // OS-specific temp directory
            String downloadPath = Paths.get(tempDir, videoId + ".mp4").toString();

            String s3Key = s3Folder + "/" + videoId + ".mp4";
            System.out.println("Attempting to download video from S3 bucket: " + s3BucketName + ", Key: " + s3Key);
            System.out.println("Resolved download path to: " + downloadPath);

            try {
                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(s3BucketName)
                        .key(s3Key)
                        .build();

                s3Client.getObject(getObjectRequest, Paths.get(downloadPath));

                File downloadedFile = new File(downloadPath);
                if (downloadedFile.exists()) {
                    System.out.println("Downloaded video successfully to " + downloadPath);
                    return downloadPath;
                } else {
                    throw new RuntimeException("Download failed, file does not exist at " + downloadPath);
                }
            } catch (Exception e) {
                System.err.println("Error downloading video from S3: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to download the video from S3", e);
            }
        }

        private String updateVideoInS3(String filePath, String s3BucketName, String s3Folder, String videoId) {
            S3Client s3Client = S3Client.builder().build();

            File file = new File(filePath);
            if (!file.exists()) {
                throw new RuntimeException("File " + filePath + " does not exist.");
            }

            String outputKey = s3Folder + "/" + videoId + "_translated.mp4";
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(s3BucketName)
                    .key(outputKey)
                    .build(),
                    RequestBody.fromFile(file));

            System.out.println("Uploaded updated video to S3 as " + outputKey);
            return "https://" + s3BucketName + ".s3.amazonaws.com/" + outputKey;
        }

        private void deleteLocalFile(String filePath) {
            File file = new File(filePath);
            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("Deleted local file: " + filePath);
                } else {
                    System.err.println("Failed to delete local file: " + filePath);
                }
            }
        }
    }
