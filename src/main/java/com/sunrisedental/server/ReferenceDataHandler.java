package com.sunrisedental.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.dao.IDentistDAO;
import com.sunrisedental.dao.ITreatmentTypeDAO;
import com.sunrisedental.util.Json;

import java.io.IOException;

/** GET /api/dentists and GET /api/treatments - reference/lookup data the console client needs to build its menus. */
public class ReferenceDataHandler implements HttpHandler {

    private final IDentistDAO dentistDAO;
    private final ITreatmentTypeDAO treatmentTypeDAO;

    public ReferenceDataHandler(IDentistDAO dentistDAO, ITreatmentTypeDAO treatmentTypeDAO) {
        this.dentistDAO = dentistDAO;
        this.treatmentTypeDAO = treatmentTypeDAO;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtil.sendMethodNotAllowed(exchange);
            return;
        }
        String path = exchange.getRequestURI().getPath();
        if (path.endsWith("/dentists")) {
            HttpUtil.sendJson(exchange, 200, Json.obj("success", true, "dentists", DtoMapper.toJsonList(dentistDAO.findAll())));
        } else if (path.endsWith("/treatments")) {
            HttpUtil.sendJson(exchange, 200, Json.obj("success", true, "treatments", DtoMapper.toJsonList(treatmentTypeDAO.findAll())));
        } else {
            HttpUtil.sendError(exchange, 404, java.util.List.of("Unknown reference data resource."));
        }
    }
}
