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

    @PostMapping("/start-download")
    public ResponseEntity<String> startDownloadProcess(@RequestParam("url") String url, 
                                                       @RequestParam("callbackUrl") String callbackUrl) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("url", url);
        variables.put("callbackUrl", callbackUrl);  // Add the callback URL to the process variables
    
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process_1ua4l8j", variables);
    
        return ResponseEntity.ok("Process started with URL: " + url + " and process instance ID: " + processInstance.getId());
    }
    

    // Endpoint to retrieve the S3 download link
    @GetMapping("/download-link")
    public ResponseEntity<Map<String, String>> getDownloadLink(@RequestParam("processInstanceId") String processInstanceId) {
        String downloadLink = (String) runtimeService.getVariable(processInstanceId, "downloadLink");

        if (downloadLink != null) {
            return ResponseEntity.ok(Collections.singletonMap("downloadLink", downloadLink));
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Collections.singletonMap("message", "Download link is not ready yet."));
        }
    }
}

