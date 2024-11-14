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
        System.out.println("URL: " + url);
        if (url == null || !url.startsWith("https://www.instagram.com/") || !url.startsWith("https://youtube.com/")) {
            validateUrl = false;
        } else {
            System.out.println("Valid Instagram URL");
            validateUrl = true;
        }

        execution.setVariable("validateUrl", validateUrl);
    }
    
}
