package com.polyglotmesh.polyglotmesh.controller;

import com.polyglotmesh.polyglotmesh.dto.CodeExecutionRequest;
import com.polyglotmesh.polyglotmesh.dto.CodeExecutionResponse;
import com.polyglotmesh.polyglotmesh.service.CodeExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ExecutionController {

    private final CodeExecutionService codeExecutionService;

    public ExecutionController(CodeExecutionService codeExecutionService) {
        this.codeExecutionService = codeExecutionService;
    }

    @PostMapping("/execute")
    public ResponseEntity<CodeExecutionResponse> execute(
            @RequestBody CodeExecutionRequest request) {

        CodeExecutionResponse response =
                codeExecutionService.execute(request);

        return ResponseEntity.ok(response);
    }
}