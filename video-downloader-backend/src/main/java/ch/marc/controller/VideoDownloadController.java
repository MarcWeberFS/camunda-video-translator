package ch.marc.controller;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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

    @GetMapping("/get-download-link")
    public ResponseEntity<?> getDownloadLink(@RequestParam("processInstanceId") String processInstanceId) {
        String downloadLink = (String) runtimeService.getVariable(processInstanceId, "downloadLink");

        if (downloadLink != null) {
            return ResponseEntity.ok(Collections.singletonMap("downloadLink", downloadLink));
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Download link is not ready yet.");
        }
    }
}
