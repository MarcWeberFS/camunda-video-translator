package ch.marc.controller;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://3.127.36.67, http://localhost:3000, http://localhost:8080")
public class VideoDownloadController {

    private static final Logger log = LoggerFactory.getLogger(VideoDownloadController.class);

    @Autowired
    private RuntimeService runtimeService;

    @PostMapping("/start-download")
    public ResponseEntity<Map<String, String>> startDownloadProcess(@RequestParam("url") String url) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("url", url);
        variables.put("advanced", true);
        variables.put("sourceLanguage", "en");
        variables.put("targetLanguage", "de");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process_1ua4l8j", variables);
        Map<String, String> response = new HashMap<>();
        response.put("processInstanceId", processInstance.getId());

        return ResponseEntity.ok(response);
    }

    @Autowired
    private HistoryService historyService;
    
    @GetMapping("/download-link")
    public ResponseEntity<Map<String, String>> getDownloadLink(@RequestParam("processInstanceId") String processInstanceId) {
        log.error("Process instance ID: {}", processInstanceId); 
    
        HistoricVariableInstance historicVariableInstance = historyService
                .createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .variableName("downloadLink")
                .singleResult();
    
        log.error("Historic Variable Instance: {}", historicVariableInstance);
    
        if (historicVariableInstance != null && historicVariableInstance.getValue() != null) {
            String downloadLink = (String) historicVariableInstance.getValue();
            return ResponseEntity.ok(Collections.singletonMap("downloadLink", downloadLink));
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Collections.singletonMap("message", "Download link is not ready yet or process not found."));
        }
    }
    

}
