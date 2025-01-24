package com.yandex.tasktracker.service.httpTaskManager;

import com.google.gson.*;
import com.yandex.tasktracker.HttpTaskServer;
import com.yandex.tasktracker.model.Status;
import com.yandex.tasktracker.model.Task;
import com.yandex.tasktracker.service.InMemoryTaskManager;
import com.yandex.tasktracker.service.TaskManager;
import com.yandex.tasktracker.service.history.InMemoryHistoryManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Обработчик истории")
public class HttpTaskManagerHistoryTest {
    TaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    @BeforeEach
    public void setUp() {
        manager.clearTasksList();
        manager.clearSubtasksList();
        manager.clearEpicsList();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    @DisplayName("получает историю просмотра")
    public void testGetHistory() throws IOException, InterruptedException {
        Task task = new Task("Задача", "1", Status.NEW, Duration.ofMinutes(5),
                LocalDateTime.of(2025, 2, 25, 18, 0));
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest postRequest = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResponse.statusCode());
        int id = manager.getTasksList().getFirst().getId();
        Task gottenTask = manager.getTask(id);

        url = URI.create("http://localhost:8080/history");
        HttpRequest getHistoryRequest = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> getHistoryResponse = client.send(getHistoryRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getHistoryResponse.statusCode());

        JsonElement jsonElement = JsonParser.parseString(getHistoryResponse.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertEquals(1, jsonArray.size());
        JsonObject taskObject = jsonArray.get(0).getAsJsonObject();
        int gottenId = taskObject.get("id").getAsInt();
        String name = taskObject.get("name").getAsString();
        String description = taskObject.get("description").getAsString();
        Status status = Status.valueOf(taskObject.get("status").getAsString());
        Duration duration = Duration.parse(taskObject.get("duration").getAsString());
        LocalDateTime startTime = LocalDateTime.parse(taskObject.get("startTime").getAsString());

        assertEquals(id, gottenId);
        assertEquals(gottenTask.getName(), name);
        assertEquals(gottenTask.getDescription(), description);
        assertEquals(gottenTask.getStatus(), status);
        assertEquals(gottenTask.getStartTime(), startTime);
        assertEquals(gottenTask.getDuration(), duration);
    }
}