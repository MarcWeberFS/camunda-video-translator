package ch.marc.listener;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DownloadLinkNotifier implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        String downloadLink = (String) execution.getVariable("downloadLink");
        String callbackUrl = (String) execution.getVariable("callbackUrl"); 
        if (downloadLink != null && callbackUrl != null) {
            RestTemplate restTemplate = new RestTemplate();
            try {
                restTemplate.postForEntity(callbackUrl, downloadLink, String.class);
                System.out.println("Download link sent to client at callback URL: " + callbackUrl);
            } catch (Exception e) {
                System.err.println("Failed to send download link: " + e.getMessage());
            }
        } else {
            System.err.println("Download link or callback URL missing");
        }
    }
}
