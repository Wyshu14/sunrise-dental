package com.sunrisedental.client;

import com.sunrisedental.util.Json;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Thin HTTP wrapper the console client uses to talk to ApiServerApp.
 * This is the "distributed" half made concrete: every method here is a
 * real network call (java.net.http.HttpClient, built into the JDK), so the
 * console client and the server can run on entirely different machines.
 */
public class ApiClient {

    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();
    private final String baseUrl;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public ApiResponse post(String path, Map<String, Object> body) {
        return send("POST", path, body);
    }

    public ApiResponse get(String path) {
        return send("GET", path, null);
    }

    private ApiResponse send(String method, String path, Map<String, Object> body) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json");

            if (body != null) {
                builder.method(method, HttpRequest.BodyPublishers.ofString(Json.write(body)));
            } else {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            }

            HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            Map<String, Object> json = Json.parseObject(response.body());
            return new ApiResponse(response.statusCode(), json);
        } catch (IOException | InterruptedException e) {
            return ApiResponse.connectionError(e.getMessage());
        }
    }
}
