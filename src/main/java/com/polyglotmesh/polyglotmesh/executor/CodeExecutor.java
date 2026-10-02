package com.polyglotmesh.polyglotmesh.executor;

import com.polyglotmesh.polyglotmesh.dto.CodeExecutionResponse;

public interface CodeExecutor {

    CodeExecutionResponse execute(String code, String input);
}