package ch.marc.controller;

import org.camunda.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class VideoDownloadController {

    @Autowired
    private RuntimeService runtimeService;

    @PostMapping("/start-download")
    public String startDownloadProcess(@RequestParam("url") String url) {

        Map<String, Object> variables = new HashMap<>();
        variables.put("url", url);

        runtimeService.startProcessInstanceByKey("Process_1ua4l8j", variables);

        return "Process started with URL: " + url;
    }
}
