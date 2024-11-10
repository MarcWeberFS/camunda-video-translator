package ch.marc.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class ValidateInstagramUrl implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String url = (String) execution.getVariable("url");
        if (url == null || !url.startsWith("https://www.instagram.com/")) {
            execution.setVariable("urlValid", true);
        } else {
            System.out.println("Valid Instagram URL");
            execution.setVariable("urlValid", false);
        }
    }
    
}
