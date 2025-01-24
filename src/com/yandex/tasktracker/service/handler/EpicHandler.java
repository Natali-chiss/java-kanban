package com.yandex.tasktracker.service.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.tasktracker.model.Epic;
import com.yandex.tasktracker.service.TaskManager;
import com.yandex.tasktracker.service.exceptions.NotFoundException;
import com.yandex.tasktracker.service.exceptions.ValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {

    public EpicHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Началась обработка /epics запроса от клиента.");
        String response = "";
        int statusCode = 200;
        try {
            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET":
                    response = handleGetRequest(exchange);
                    break;
                case "POST":
                    response = handlePostRequest(exchange);
                    statusCode = 201;
                    break;
                case "DELETE":
                    response = handleDeleteRequest(exchange);
                    break;
                default:
                    response = "Некорректный метод";
                    statusCode = 400;
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
        if (path.endsWith("epics") && split.length == 2) {
            return gson.toJson(manager.getEpicsList());
        } else if (split.length == 3 && split[2] != null) {
            Integer id = Integer.parseInt(split[2]);
            Epic epic = manager.getEpic(id);
            return gson.toJson(epic);
        } else if (split.length == 4 && path.endsWith("subtasks") && split[2] != null) {
            Integer id = Integer.parseInt(split[2]);
            return gson.toJson(manager.getEpicSubtasks(id));
        } else {
            return gson.toJson("Некорректный запрос");
        }
    }

    private static String handlePostRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] split = path.split("/");
        InputStream inputStream = exchange.getRequestBody();
        String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        Epic epic = gson.fromJson(body, Epic.class);
        if (path.endsWith("epics") && split.length == 2) {
            manager.createEpic(epic);
            return gson.toJson("Создан эпик с id: " + epic.getId());
        } else if (split.length == 3 && split[2] != null) {
            int id = Integer.parseInt(split[2]);
            epic.setId(id);
            manager.updateEpic(epic);
            return gson.toJson("Эпик обновлен");
        } else {
            return gson.toJson("Некорректный запрос");
        }
    }

    private static String handleDeleteRequest(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        String[] split = path.split("/");
        if (path.endsWith("epics") && split.length == 2) {
            manager.clearEpicsList();
            return gson.toJson("Список эпиков удалён");
        } else if (split.length == 3 && split[2] != null) {
            Integer id = Integer.parseInt(split[2]);
            manager.removeEpic(id);
            return gson.toJson("Удален эпик с id " + id);
        } else {
            return gson.toJson("Некорректный запрос");
        }
    }
}