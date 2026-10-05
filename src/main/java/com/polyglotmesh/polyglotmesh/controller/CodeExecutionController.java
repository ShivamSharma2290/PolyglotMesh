package com.polyglotmesh.polyglotmesh.controller;

import com.polyglotmesh.polyglotmesh.dto.CodeExecutionRequest;
import com.polyglotmesh.polyglotmesh.dto.CodeExecutionResponse;
import com.polyglotmesh.polyglotmesh.service.CodeExecutionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/code")
public class CodeExecutionController {

    private final CodeExecutionService codeExecutionService;

    public CodeExecutionController(CodeExecutionService codeExecutionService) {
        this.codeExecutionService = codeExecutionService;
    }

    @PostMapping("/execute")
    public CodeExecutionResponse execute(
            @RequestBody CodeExecutionRequest request) {

        return codeExecutionService.execute(request);
    }
}