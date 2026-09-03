package com.sunrisedental.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.domain.Appointment;
import com.sunrisedental.service.AppointmentRegistrationRequest;
import com.sunrisedental.service.AppointmentService;
import com.sunrisedental.util.Json;
import com.sunrisedental.util.ServiceResult;

import java.io.IOException;
import java.util.Map;

/**
 * Handles the appointment resource:
 *   POST /api/appointments                       -> register new appointment
 *   GET  /api/appointments/{number}               -> search/display appointment
 *   POST /api/appointments/{number}/reschedule     -> update appointment
 *   POST /api/appointments/{number}/cancel         -> cancel appointment
 */
public class AppointmentHandler implements HttpHandler {

    private static final String PREFIX = "/api/appointments/";

    private final AppointmentService appointmentService;

    public AppointmentHandler(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (path.equals("/api/appointments") && "POST".equalsIgnoreCase(method)) {
            handleRegister(exchange);
            return;
        }

        String suffix = HttpUtil.pathSuffix(exchange, PREFIX); // "{number}" or "{number}/reschedule" or "{number}/cancel"
        if (suffix.endsWith("/reschedule") && "POST".equalsIgnoreCase(method)) {
            String number = suffix.substring(0, suffix.length() - "/reschedule".length());
            handleReschedule(exchange, number);
        } else if (suffix.endsWith("/cancel") && "POST".equalsIgnoreCase(method)) {
            String number = suffix.substring(0, suffix.length() - "/cancel".length());
            handleCancel(exchange, number);
        } else if (!suffix.isBlank() && "GET".equalsIgnoreCase(method)) {
            handleSearch(exchange, suffix);
        } else {
            HttpUtil.sendMethodNotAllowed(exchange);
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        Map<String, Object> body = Json.parseObject(HttpUtil.readBody(exchange));
        AppointmentRegistrationRequest req = new AppointmentRegistrationRequest(
            HttpUtil.getString(body, "patientName"),
            HttpUtil.getString(body, "address"),
            HttpUtil.getString(body, "contactNumber"),
            HttpUtil.getInt(body, "dentistId"),
            HttpUtil.getInt(body, "treatmentId"),
            HttpUtil.getString(body, "date"),
            HttpUtil.getString(body, "time")
        );
        ServiceResult<Appointment> result = appointmentService.registerAppointment(req);
        respond(exchange, result, 201);
    }

    private void handleSearch(HttpExchange exchange, String number) throws IOException {
        ServiceResult<Appointment> result = appointmentService.searchByNumber(number);
        respond(exchange, result, 200);
    }

    private void handleReschedule(HttpExchange exchange, String number) throws IOException {
        Map<String, Object> body = Json.parseObject(HttpUtil.readBody(exchange));
        ServiceResult<Appointment> result = appointmentService.reschedule(
            number, HttpUtil.getString(body, "date"), HttpUtil.getString(body, "time"));
        respond(exchange, result, 200);
    }

    private void handleCancel(HttpExchange exchange, String number) throws IOException {
        ServiceResult<Appointment> result = appointmentService.cancel(number);
        respond(exchange, result, 200);
    }

    private void respond(HttpExchange exchange, ServiceResult<Appointment> result, int successStatus) throws IOException {
        if (result.isSuccess()) {
            HttpUtil.sendJson(exchange, successStatus, Json.obj("success", true, "appointment", DtoMapper.toJson(result.getData())));
        } else {
            HttpUtil.sendError(exchange, 400, result.getErrors());
        }
    }
}
