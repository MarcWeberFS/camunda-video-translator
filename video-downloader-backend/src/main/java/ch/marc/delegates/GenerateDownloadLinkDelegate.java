package ch.marc.delegates;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class GenerateDownloadLinkDelegate implements JavaDelegate{

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String downloadResult = (String) execution.getVariable("downloadResult");

        String regex = "(https://[\\w\\-\\.]+\\.s3\\.amazonaws\\.com/[\\w\\-\\/\\.]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(downloadResult);

        String downloadLink = null;

        if (matcher.find()) {
            downloadLink = matcher.group(1);
        }

        if (downloadLink == null) {
            System.err.println("No S3 URL found in downloadResult :()");
        } else {
            execution.setVariable("downloadLink", downloadLink);
            System.out.println("Extracted S3 URL:: " + downloadLink);
        }
    }
    
}
