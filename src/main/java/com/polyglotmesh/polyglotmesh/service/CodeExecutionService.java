package com.polyglotmesh.polyglotmesh.service;

import com.polyglotmesh.polyglotmesh.dto.CodeExecutionRequest;
import com.polyglotmesh.polyglotmesh.dto.CodeExecutionResponse;
import com.polyglotmesh.polyglotmesh.executor.JavaExecutor;
import com.polyglotmesh.polyglotmesh.executor.JavaScriptExecutor;
import com.polyglotmesh.polyglotmesh.executor.PythonExecutor;
import org.springframework.stereotype.Service;

@Service
public class CodeExecutionService {

    private final JavaExecutor javaExecutor;
    private final PythonExecutor pythonExecutor;
    private final JavaScriptExecutor javaScriptExecutor;

    public CodeExecutionService(
            JavaExecutor javaExecutor,
            PythonExecutor pythonExecutor,
            JavaScriptExecutor javaScriptExecutor) {

        this.javaExecutor = javaExecutor;
        this.pythonExecutor = pythonExecutor;
        this.javaScriptExecutor = javaScriptExecutor;
    }

    public CodeExecutionResponse execute(CodeExecutionRequest request) {

        if (request.getLanguage() == null ||
                request.getLanguage().isBlank()) {

            return new CodeExecutionResponse(
                    "",
                    "ERROR",
                    "",
                    "Language is required",
                    0
            );
        }

        if (request.getCode() == null ||
                request.getCode().isBlank()) {

            return new CodeExecutionResponse(
                    request.getLanguage(),
                    "ERROR",
                    "",
                    "Code is required",
                    0
            );
        }

        String language = request.getLanguage().toLowerCase();

        if (language.equals("java")) {

            return javaExecutor.execute(
                    request.getCode(),
                    request.getInput()
            );
        }

        if (language.equals("python")) {

            return pythonExecutor.execute(
                    request.getCode(),
                    request.getInput()
            );
        }

        if (language.equals("javascript") ||
                language.equals("js")) {

            return javaScriptExecutor.execute(
                    request.getCode(),
                    request.getInput()
            );
        }

        return new CodeExecutionResponse(
                language,
                "ERROR",
                "",
                "Language not supported yet: " + language,
                0
        );
    }
}