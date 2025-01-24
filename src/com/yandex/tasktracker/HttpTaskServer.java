package com.yandex.tasktracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

import com.yandex.tasktracker.service.adapter.DurationAdapter;
import com.yandex.tasktracker.service.adapter.LocalDateTimeAdapter;
import com.yandex.tasktracker.service.Managers;
import com.yandex.tasktracker.service.TaskManager;
import com.yandex.tasktracker.service.handler.*;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer httpServer;

    Gson gson;

    public HttpTaskServer() {
        this(Managers.getDefault());
    }

    public HttpTaskServer(TaskManager manager) {
        this.gson = getGson();
        try {
            httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        httpServer.createContext("/tasks", new TaskHandler(manager, gson));
        httpServer.createContext("/epics", new EpicHandler(manager, gson));
        httpServer.createContext("/subtasks", new SubtaskHandler(manager, gson));
        httpServer.createContext("/history", new HistoryHandler(manager, gson));
        httpServer.createContext("/prioritized", new PrioritizedTaskHandler(manager, gson));
    }

    public void start() {
        httpServer.start();
        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }

    public void stop() {
        httpServer.stop(0);
        System.out.println("Остановили работу HTTP-сервера на " + PORT + " порту!");
    }

    public static Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationAdapter());
        return gsonBuilder.create();
    }

    public static void main(String[] args) {
        HttpTaskServer httpServer = new HttpTaskServer();
        System.out.println("Запуск сервера");
        httpServer.start();
        httpServer.stop();
    }
}