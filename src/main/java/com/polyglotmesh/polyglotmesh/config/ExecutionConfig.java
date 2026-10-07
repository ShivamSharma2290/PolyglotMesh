package com.polyglotmesh.polyglotmesh.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ExecutionConfig {

    // Maximum execution time in milliseconds
    private final long maxExecutionTime = 5000;

    // Maximum output size in bytes
    private final int maxOutputSize = 10 * 1024;

    public long getMaxExecutionTime() {
        return maxExecutionTime;
    }

    public int getMaxOutputSize() {
        return maxOutputSize;
    }
}