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
    public ResponseEntity<Map<String, String>> startDownloadProcess(@RequestParam("url") String url) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("url", url);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process_1ua4l8j", variables);
        Map<String, String> response = new HashMap<>();
        response.put("processInstanceId", processInstance.getId());

        return ResponseEntity.ok(response);
    }

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
