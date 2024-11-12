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
        String sourceLanguage = (String) execution.getVariable("sourceLanguage");

        String s3Uri = downloadLink.replace("https://video-download-temp.s3.amazonaws.com/", "s3://video-download-temp/");
        String videoId = s3Uri.substring(s3Uri.lastIndexOf("/") + 1, s3Uri.lastIndexOf("."));
        String jobName = "TranscriptionJob_" + videoId;

        execution.setVariable("videoId", videoId);

        TranscribeClient transcribeClient = TranscribeClient.builder().build();

        StartTranscriptionJobRequest transcriptionJobRequest = StartTranscriptionJobRequest.builder()
            .transcriptionJobName(jobName)
            .languageCode(getLanguageCodeByString(sourceLanguage))
            .mediaFormat(MediaFormat.MP4)
            .media(Media.builder().mediaFileUri(s3Uri).build())
            .subtitles(Subtitles.builder().formats(SubtitleFormat.SRT).build())
            .outputBucketName("video-download-temp")
            .build();

        try {
            StartTranscriptionJobResponse transcriptionJobResponse = transcribeClient.startTranscriptionJob(transcriptionJobRequest);
            System.out.println("Transcription job started with ID: " + transcriptionJobResponse.transcriptionJob().transcriptionJobName());

            execution.setVariable("transcriptionJobId", transcriptionJobResponse.transcriptionJob().transcriptionJobName());

            while (true) {
                GetTranscriptionJobRequest getJobRequest = GetTranscriptionJobRequest.builder()
                        .transcriptionJobName(jobName)
                        .build();

                TranscriptionJobStatus jobStatus = transcribeClient.getTranscriptionJob(getJobRequest).transcriptionJob().transcriptionJobStatus();
                if (jobStatus == TranscriptionJobStatus.COMPLETED) {
                    System.out.println("Transcription job completed.");
                    break;
                } else if (jobStatus == TranscriptionJobStatus.FAILED) {
                    throw new RuntimeException("Transcription job failed.");
                }

                System.out.println("Waiting for transcription job to complete...");
                Thread.sleep(5000);
            }

        } catch (TranscribeException e) {
            System.err.println("Error starting transcription job: " + e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Transcription job failed", e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            transcribeClient.close();
        }
    }

    private LanguageCode getLanguageCodeByString (String sourceLanguage) {
        switch (sourceLanguage) {
            case "en":
                return LanguageCode.EN_US;
            case "de":
                return LanguageCode.DE_DE;
            case "fr":
                return LanguageCode.FR_FR;
            case "es":
                return LanguageCode.ES_US;
            case "it":
                return LanguageCode.IT_IT;
            case "pt":
                return LanguageCode.PT_BR;
            case "nl":
                return LanguageCode.NL_NL;
            case "ru":
                return LanguageCode.RU_RU;
            case "zh":
                return LanguageCode.ZH_CN;
            case "ja": 
                return LanguageCode.JA_JP;
            case "ko":
                return LanguageCode.KO_KR;
            case "ar":
                return LanguageCode.AR_SA;
            case "hi":
                return LanguageCode.HI_IN;
            case "tr":
                return LanguageCode.TR_TR;
            case "pl":
                return LanguageCode.PL_PL;
            case "sv":
                return LanguageCode.SV_SE;
            default:
                return LanguageCode.EN_US;
        }
    }
}
