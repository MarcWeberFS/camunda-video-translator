package ch.marc.delegates;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;

@Component
public class TranslateVideoDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String videoId = (String) execution.getVariable("videoId");
        String sourceLanguage = (String) execution.getVariable("sourceLanguage");
        String targetLanguage = (String) execution.getVariable("targetLanguage");
        
        String bucketName = "video-download-temp";
        String s3Key = "TranscriptionJob_" + videoId + ".srt";

        S3Client s3Client = S3Client.builder().build();
        TranslateClient translateClient = TranslateClient.builder().build();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        try (ResponseInputStream s3ObjectStream = s3Client.getObject(getObjectRequest);
             BufferedReader reader = new BufferedReader(new InputStreamReader(s3ObjectStream))) {

            String srtContent = reader.lines().collect(Collectors.joining("\n"));
            System.out.println("Original SRT content:" + srtContent);

            TranslateTextRequest translateTextRequest = TranslateTextRequest.builder()
                    .sourceLanguageCode(sourceLanguage)
                    .targetLanguageCode(targetLanguage)
                    .text(srtContent)
                    .build();

            TranslateTextResponse translateTextResponse = translateClient.translateText(translateTextRequest);
            String translatedText = translateTextResponse.translatedText();

            System.out.println("Translated SRT content:\n" + translatedText);
            execution.setVariable("translatedText", translatedText);

        } catch (Exception e) {
            System.err.println("Error processing translation: " + e.getMessage());
            throw new RuntimeException("Translation failed", e);
        } finally {
            s3Client.close();
            translateClient.close();
        }
    }
}