package com.yandex.tasktracker.service.handler;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.tasktracker.model.Status;
import com.yandex.tasktracker.model.Task;
import com.yandex.tasktracker.service.TaskManager;
import com.yandex.tasktracker.service.exceptions.ManagerSaveException;
import com.yandex.tasktracker.service.exceptions.NotFoundException;
import com.yandex.tasktracker.service.exceptions.ValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {

    public TaskHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Началась обработка /tasks запроса от клиента.");

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
        } catch (ManagerSaveException exception) {
            sendInternalServerError(exchange, exception.getMessage());
        }
        sendText(exchange, response, statusCode);
    }

    private String handleGetRequest(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        String[] split = path.split("/");
        if (path.endsWith("tasks") && split.length == 2) {
            return gson.toJson(manager.getTasksList());
        } else if (split.length == 3 && split[2] != null) {
            Integer id = Integer.parseInt(split[2]);
            Task task = manager.getTask(id);
            return gson.toJson(task);
        } else {
            return gson.toJson("Некорректный запрос");
        }
    }

    private static String handlePostRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] split = path.split("/");
        InputStream inputStream = exchange.getRequestBody();
        String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        Task task = gson.fromJson(body, Task.class);
        if (task.getStatus() == null) {
            task.setStatus(Status.NEW);
        }
        if (path.endsWith("tasks") && split.length == 2) {
            manager.createTask(task);
            return gson.toJson("Создана задача с id: " + task.getId());
        } else if (split.length == 3 && split[2] != null) {
            int id = Integer.parseInt(split[2]);
            task.setId(id);
            manager.updateTask(task);
            return gson.toJson("Задача обновлена");
        } else {
            return gson.toJson("Неверный путь");
        }
    }

    private static String handleDeleteRequest(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        String[] split = path.split("/");
        if (path.endsWith("tasks") && split.length == 2) {
            manager.clearTasksList();
            return gson.toJson("Список задач удалён");
        } else if (split.length == 3 && split[2] != null) {
            Integer id = Integer.parseInt(split[2]);
            manager.removeTask(id);
            return gson.toJson("Удалена задача с id " + id);
        } else {
            return gson.toJson("Некорректный запрос");
        }
    }
}