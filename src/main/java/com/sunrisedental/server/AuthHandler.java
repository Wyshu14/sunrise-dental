package com.sunrisedental.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.domain.User;
import com.sunrisedental.service.AuthenticationService;
import com.sunrisedental.util.Json;
import com.sunrisedental.util.ServiceResult;

import java.io.IOException;
import java.util.Map;

/** POST /api/login  { "username": "...", "password": "..." } */
public class AuthHandler implements HttpHandler {

    private final AuthenticationService authService;

    public AuthHandler(AuthenticationService authService) {
        this.authService = authService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtil.sendMethodNotAllowed(exchange);
            return;
        }
        Map<String, Object> body = Json.parseObject(HttpUtil.readBody(exchange));
        String username = HttpUtil.getString(body, "username");
        String password = HttpUtil.getString(body, "password");

        ServiceResult<User> result = authService.login(username, password);
        if (result.isSuccess()) {
            HttpUtil.sendJson(exchange, 200, Json.obj("success", true, "user", DtoMapper.toJson(result.getData())));
        } else {
            HttpUtil.sendError(exchange, 401, result.getErrors());
        }
    }
}
