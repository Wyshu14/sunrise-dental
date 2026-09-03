package com.sunrisedental.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.service.ReportService;
import com.sunrisedental.util.Json;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

/**
 * GET /api/reports/daily?date=YYYY-MM-DD
 * GET /api/reports/revenue?from=YYYY-MM-DD&to=YYYY-MM-DD
 * Administrator-only in the console client's menu gating (server also usable directly for testing).
 */
public class ReportHandler implements HttpHandler {

    private final ReportService reportService;

    public ReportHandler(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtil.sendMethodNotAllowed(exchange);
            return;
        }
        String path = exchange.getRequestURI().getPath();
        Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());

        if (path.endsWith("/daily")) {
            LocalDate date = query.containsKey("date") ? LocalDate.parse(query.get("date")) : LocalDate.now();
            String report = reportService.dailyAppointmentsReport(date);
            HttpUtil.sendJson(exchange, 200, Json.obj("success", true, "report", report));
        } else if (path.endsWith("/revenue")) {
            LocalDate from = query.containsKey("from") ? LocalDate.parse(query.get("from")) : LocalDate.now().withDayOfMonth(1);
            LocalDate to = query.containsKey("to") ? LocalDate.parse(query.get("to")) : LocalDate.now();
            String report = reportService.revenueReport(from, to);
            HttpUtil.sendJson(exchange, 200, Json.obj("success", true, "report", report));
        } else {
            HttpUtil.sendError(exchange, 404, java.util.List.of("Unknown report."));
        }
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new java.util.HashMap<>();
        if (query == null) return map;
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                map.put(parts[0], java.net.URLDecoder.decode(parts[1], java.nio.charset.StandardCharsets.UTF_8));
            }
        }
        return map;
    }
}
