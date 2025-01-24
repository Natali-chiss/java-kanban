package com.yandex.tasktracker.service.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.tasktracker.model.Subtask;
import com.yandex.tasktracker.service.TaskManager;
import com.yandex.tasktracker.service.exceptions.NotFoundException;
import com.yandex.tasktracker.service.exceptions.ValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {

    public SubtaskHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Началась обработка /subtasks запроса от клиента.");
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
        if (path.endsWith("subtasks") && split.length == 2) {
            return gson.toJson(manager.getSubtasksList());
        } else if (split.length == 3 && split[2] != null) {
            Integer id = Integer.parseInt(split[2]);
            Subtask subtask = manager.getSubtask(id);
            return gson.toJson(subtask);
        } else {
            return gson.toJson("Некорректный запрос");
        }
    }

    private static String handlePostRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] split = path.split("/");
        InputStream inputStream = exchange.getRequestBody();
        String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        Subtask subtask = gson.fromJson(body, Subtask.class);
        if (path.endsWith("subtasks") && split.length == 2) {
            manager.createSubtask(subtask);
            System.out.println(subtask);
            return gson.toJson("Создана подзадача с id: " + subtask.getId());
        } else if (split.length == 3 && split[2] != null) {
            int id = Integer.parseInt(split[2]);
            subtask.setId(id);
            manager.updateSubtask(subtask);
            return gson.toJson("Подзадача обновлена");
        } else {
            return gson.toJson("Некорректный запрос");
        }
    }

    private static String handleDeleteRequest(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        String[] split = path.split("/");
        if (path.endsWith("subtasks") && split.length == 2) {
            manager.clearSubtasksList();
            return gson.toJson("Список подзадач удалён");
        } else if (split.length == 3 && split[2] != null) {
            Integer id = Integer.parseInt(split[2]);
            manager.removeSubtask(id);
            return gson.toJson("Удалена подзадача с id " + id);
        } else {
            return gson.toJson("Некорректный запрос");
        }
    }
}