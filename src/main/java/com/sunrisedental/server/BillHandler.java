package com.sunrisedental.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.domain.Bill;
import com.sunrisedental.service.BillService;
import com.sunrisedental.util.Json;
import com.sunrisedental.util.ServiceResult;

import java.io.IOException;
import java.util.Map;

/** POST /api/bills/{appointmentNumber}   body: { "discountAmount": 0 } */
public class BillHandler implements HttpHandler {

    private static final String PREFIX = "/api/bills/";

    private final BillService billService;

    public BillHandler(BillService billService) {
        this.billService = billService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtil.sendMethodNotAllowed(exchange);
            return;
        }
        String appointmentNumber = HttpUtil.pathSuffix(exchange, PREFIX);
        Map<String, Object> body = Json.parseObject(HttpUtil.readBody(exchange));
        double discount = HttpUtil.getDouble(body, "discountAmount", 0.0);

        ServiceResult<Bill> result = billService.generateBill(appointmentNumber, discount);
        if (result.isSuccess()) {
            HttpUtil.sendJson(exchange, 201, Json.obj("success", true, "bill", DtoMapper.toJson(result.getData())));
        } else {
            HttpUtil.sendError(exchange, 400, result.getErrors());
        }
    }
}
