package ch.marc.controller;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://3.127.36.67")
public class VideoDownloadController {

    @Autowired
    private RuntimeService runtimeService;

    // Store download links in a temporary map (use a more persistent store in production)
    private Map<String, String> downloadLinks = new HashMap<>();

    @PostMapping("/start-download")
    public String startDownloadProcess(@RequestParam("url") String url) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("url", url);
        variables.put("callbackUrl", "http://3.127.36.67/api/download-link-notification");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process_1ua4l8j", variables);

        return "Process started with URL: " + url + " and process instance ID: " + processInstance.getId();
    }

    @PostMapping("/download-link-notification")
    public void receiveDownloadLink(@RequestParam("downloadLink") String downloadLink, 
                                    @RequestParam("processInstanceId") String processInstanceId) {
        downloadLinks.put(processInstanceId, downloadLink);
    }

    @GetMapping("/download-link")
    public Map<String, String> getDownloadLink(@RequestParam("processInstanceId") String processInstanceId) {
        Map<String, String> response = new HashMap<>();
        response.put("downloadLink", downloadLinks.get(processInstanceId));
        return response;
    }
}
