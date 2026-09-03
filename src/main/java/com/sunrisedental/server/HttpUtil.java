package com.sunrisedental.server;

import com.sun.net.httpserver.HttpExchange;
import com.sunrisedental.util.Json;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/** Shared helpers for reading requests and writing JSON responses from the built-in JDK HTTP server. */
public final class HttpUtil {

    private HttpUtil() { }

    public static String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    public static void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        byte[] bytes = Json.write(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void sendError(HttpExchange exchange, int statusCode, List<String> errors) throws IOException {
        sendJson(exchange, statusCode, Json.obj("success", false, "errors", errors));
    }

    public static void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        sendError(exchange, 405, List.of("Method not allowed."));
    }

    /** Extracts the path segment after the given prefix, e.g. "/api/appointments/APT-1" with prefix "/api/appointments/" -> "APT-1". */
    public static String pathSuffix(HttpExchange exchange, String prefix) {
        String path = exchange.getRequestURI().getPath();
        if (path.length() <= prefix.length()) return "";
        return path.substring(prefix.length());
    }

    @SuppressWarnings("unchecked")
    public static String getString(Map<String, Object> json, String key) {
        Object v = json.get(key);
        return v == null ? null : String.valueOf(v);
    }

    public static int getInt(Map<String, Object> json, String key) {
        Object v = json.get(key);
        if (v == null) return 0;
        return (int) Math.round((Double) v);
    }

    public static double getDouble(Map<String, Object> json, String key, double defaultValue) {
        Object v = json.get(key);
        if (v == null) return defaultValue;
        return (Double) v;
    }
}
