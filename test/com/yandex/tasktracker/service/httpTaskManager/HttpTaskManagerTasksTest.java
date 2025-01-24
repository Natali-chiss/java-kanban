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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Обработчик задач")
public class HttpTaskManagerTasksTest {
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
    @DisplayName("добавляет задачу")
    public void testAddTask() throws IOException, InterruptedException {
        Task task = new Task("Задача", "1", Status.NEW, Duration.ofMinutes(5),
                LocalDateTime.of(2025, 2, 25, 18, 0));
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getTasksList();
        Task createdTask = tasksFromManager.getFirst();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Задача", createdTask.getName(), "Некорректное имя задачи");
        assertEquals("1", createdTask.getDescription(), "Некорректное описание задачи");
        assertEquals(Status.NEW, createdTask.getStatus(), "Некорректный статус задачи");
        assertEquals("PT5M", createdTask.getDuration().toString(), "Некорректная длительность задачи");
        assertEquals(LocalDateTime.of(2025, 2, 25, 18, 0),
                createdTask.getStartTime(), "Некорректное время начала задачи");
    }

    @Test
    @DisplayName("обновляет задачу")
    public void testUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("Задача", "1", Status.NEW, Duration.ofMinutes(5),
                LocalDateTime.of(2025, 2, 25, 18, 0));
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        int id = manager.getTasksList().getFirst().getId();

        Task newTask = new Task("Измененная задача", "1", Status.IN_PROGRESS, Duration.ofMinutes(10),
                LocalDateTime.of(2025, 2, 26, 18, 0));
        String newTaskJson = gson.toJson(newTask);

        url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest updateRequest = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers
                .ofString(newTaskJson)).build();

        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, updateResponse.statusCode());

        Task updatedTask = manager.getTask(id);
        assertEquals(newTask.getName(), updatedTask.getName(), "Некорректное имя задачи");
        assertEquals(newTask.getDescription(), updatedTask.getDescription(), "Некорректное описание задачи");
        assertEquals(newTask.getStatus(), updatedTask.getStatus(), "Некорректный статус задачи");
        assertEquals(newTask.getDuration(), updatedTask.getDuration(), "Некорректная длительность задачи");
        assertEquals(newTask.getStartTime(), updatedTask.getStartTime(), "Некорректное время начала задачи");
    }

    @Test
    @DisplayName("удаляет задачу")
    public void testRemoveTask() throws IOException, InterruptedException {
        Task task = new Task("Задача", "1", Status.NEW);
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest postRequest1 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response1 = client.send(postRequest1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response1.statusCode());

        HttpRequest deleteListRequest = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> deleteListResponse = client.send(deleteListRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteListResponse.statusCode());
        assertEquals(0, manager.getTasksList().size(), "Список задач не удален");

        HttpRequest postRequest2 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> postResponse2 = client.send(postRequest2, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResponse2.statusCode());
        int id = manager.getTasksList().getFirst().getId();

        url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest deleteTaskRequest = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> deleteTaskResponse = client.send(deleteTaskRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteTaskResponse.statusCode());
        assertEquals(0, manager.getTasksList().size(), "Задача не удалена");
    }

    @Test
    @DisplayName("получает задачу")
    public void testGetTask() throws IOException, InterruptedException {
        Task task = new Task("Задача", "1", Status.NEW, Duration.ofMinutes(5),
                LocalDateTime.of(2025, 2, 25, 18, 0));
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest postRequest = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResponse.statusCode());
        Task createdTask = manager.getTasksList().getFirst();
        int id = createdTask.getId();

        HttpRequest getRequest = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());

        JsonElement jsonElement = JsonParser.parseString(getResponse.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        JsonObject taskObject = jsonArray.get(0).getAsJsonObject();
        int gottenId = taskObject.get("id").getAsInt();
        String name = taskObject.get("name").getAsString();
        String description = taskObject.get("description").getAsString();
        Status status = Status.valueOf(taskObject.get("status").getAsString());
        Duration duration = Duration.parse(taskObject.get("duration").getAsString());
        LocalDateTime startTime = LocalDateTime.parse(taskObject.get("startTime").getAsString());

        assertEquals(id, gottenId);
        assertEquals(createdTask.getName(), name);
        assertEquals(createdTask.getDescription(), description);
        assertEquals(createdTask.getStatus(), status);
        assertEquals(createdTask.getStartTime(), startTime);
        assertEquals(createdTask.getDuration(), duration);

        url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest getTaskRequest = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> getTaskResponse = client.send(getTaskRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getTaskResponse.statusCode());

        jsonElement = JsonParser.parseString(getTaskResponse.body());
        taskObject = jsonElement.getAsJsonObject();
        gottenId = taskObject.get("id").getAsInt();
        name = taskObject.get("name").getAsString();
        description = taskObject.get("description").getAsString();
        status = Status.valueOf(taskObject.get("status").getAsString());
        duration = Duration.parse(taskObject.get("duration").getAsString());
        startTime = LocalDateTime.parse(taskObject.get("startTime").getAsString());

        assertEquals(id, gottenId);
        assertEquals(createdTask.getName(), name);
        assertEquals(createdTask.getDescription(), description);
        assertEquals(createdTask.getStatus(), status);
        assertEquals(createdTask.getStartTime(), startTime);
        assertEquals(createdTask.getDuration(), duration);
    }
}