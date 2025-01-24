package com.yandex.tasktracker.service.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.tasktracker.service.TaskManager;
import com.yandex.tasktracker.service.exceptions.NotFoundException;
import com.yandex.tasktracker.service.exceptions.ValidationException;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {

    public HistoryHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Началась обработка /history запроса от клиента.");
        String response = "";
        int statusCode = 200;
        try {
            String method = exchange.getRequestMethod();
            if (method.equals("GET")) {
                response = handleGetRequest(exchange);
            } else {
                response = "Некорректный метод";
            }
        } catch (NotFoundException exception) {
            sendNotFound(exchange, exception.getMessage());
        } catch (ValidationException exception) {
            sendHasInteractions(exchange, exception.getMessage());
        }
        sendText(exchange, response, statusCode);
    }

    private String handleGetRequest(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        String[] split = path.split("/");
        if (path.endsWith("history") && split.length == 2) {
            return gson.toJson(manager.getHistory());
        } else {
            return gson.toJson("Некорректный запрос");
        }
    }
}