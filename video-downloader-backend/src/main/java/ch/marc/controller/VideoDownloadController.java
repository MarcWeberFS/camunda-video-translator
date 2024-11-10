package ch.marc.controller;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class VideoDownloadController {
    
    @Autowired
    private RuntimeService runtimeService;

    @PostMapping("/start-download")
    public String startDownload(@RequestParam("url") String url) {

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("url", url);
        
        return "Process with the ID " + processInstance.getId() + " started.";

    }
}
