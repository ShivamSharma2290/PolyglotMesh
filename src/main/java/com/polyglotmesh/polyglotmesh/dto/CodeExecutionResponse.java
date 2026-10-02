package com.polyglotmesh.polyglotmesh.dto;

public class CodeExecutionResponse {

    private String language;
    private String status;
    private String output;
    private String error;
    private long executionTime;

    public CodeExecutionResponse() {
    }

    public CodeExecutionResponse(
            String language,
            String status,
            String output,
            String error,
            long executionTime) {

        this.language = language;
        this.status = status;
        this.output = output;
        this.error = error;
        this.executionTime = executionTime;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }
}