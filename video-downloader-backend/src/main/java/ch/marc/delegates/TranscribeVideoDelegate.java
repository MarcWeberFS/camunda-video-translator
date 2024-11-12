package ch.marc.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.*;

@Component
public class TranscribeVideoDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        String downloadLink = (String) execution.getVariable("downloadLink");

        String s3Uri = downloadLink.replace("https://video-download-temp.s3.amazonaws.com/", "s3://video-download-temp/");
        String videoId = s3Uri.substring(s3Uri.lastIndexOf("/") + 1, s3Uri.lastIndexOf("."));

        execution.setVariable("videoId", videoId);

        TranscribeClient transcribeClient = TranscribeClient.builder().build();

        StartTranscriptionJobRequest transcriptionJobRequest = StartTranscriptionJobRequest.builder()
            .transcriptionJobName("TranscriptionJob_" + videoId)
            .languageCode(LanguageCode.EN_US)
            .mediaFormat(MediaFormat.MP4)
            .media(Media.builder().mediaFileUri(s3Uri).build())
            .subtitles(Subtitles.builder().formats(SubtitleFormat.SRT).build())
            .build();

        try {
            StartTranscriptionJobResponse transcriptionJobResponse = transcribeClient.startTranscriptionJob(transcriptionJobRequest);
            System.out.println("Transcription job started with ID: " + transcriptionJobResponse.transcriptionJob().transcriptionJobName());

            execution.setVariable("transcriptionJobId", transcriptionJobResponse.transcriptionJob().transcriptionJobName());

        } catch (TranscribeException e) {
            System.err.println("Error starting transcription job: " + e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Transcription job failed", e);
        } finally {
            transcribeClient.close();
        }
    }
}
