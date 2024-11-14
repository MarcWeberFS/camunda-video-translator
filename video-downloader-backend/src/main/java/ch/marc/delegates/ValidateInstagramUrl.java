package ch.marc.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class ValidateInstagramUrl implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String url = (String) execution.getVariable("url");
        boolean validateUrl = false;
        String urlType = "";
        System.out.println("URL: " + url);
        if (url.startsWith("https://www.instagram.com/")) {
            urlType = "instagram";
            System.out.println("Instagram URL detected");
            validateUrl = true;
        } else if (url.startsWith("https://youtube.com/")) {
            urlType = "youtube";
            System.out.println("Youtube URL detected");
            validateUrl = true;
        } else {
            System.out.println("Invalid URL: Doesnt match Instagram or Youtube URL");
            validateUrl = false;
        }
        execution.setVariable("urlType", urlType);
        execution.setVariable("validateUrl", validateUrl);
    }
    
}
