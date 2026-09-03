package com.sunrisedental.client;

import java.util.List;
import java.util.Map;

/** Wraps an HTTP response from the API server for the console client to inspect. */
public class ApiResponse {

    private final int statusCode;
    private final Map<String, Object> body;
    private final boolean connectionError;
    private final String connectionErrorMessage;

    public ApiResponse(int statusCode, Map<String, Object> body) {
        this.statusCode = statusCode;
        this.body = body;
        this.connectionError = false;
        this.connectionErrorMessage = null;
    }

    private ApiResponse(String errorMessage) {
        this.statusCode = -1;
        this.body = Map.of();
        this.connectionError = true;
        this.connectionErrorMessage = errorMessage;
    }

    public static ApiResponse connectionError(String message) {
        return new ApiResponse(message);
    }

    public boolean isConnectionError() { return connectionError; }
    public String getConnectionErrorMessage() { return connectionErrorMessage; }

    public boolean isSuccess() {
        return !connectionError && statusCode >= 200 && statusCode < 300;
    }

    public int getStatusCode() { return statusCode; }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getData(String key) {
        return (Map<String, Object>) body.get(key);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getList(String key) {
        return (List<Map<String, Object>>) body.get(key);
    }

    public String getString(String key) {
        Object v = body.get(key);
        return v == null ? null : String.valueOf(v);
    }

    @SuppressWarnings("unchecked")
    public List<String> getErrors() {
        Object errors = body.get("errors");
        return errors == null ? List.of("Unknown error.") : (List<String>) errors;
    }
}
