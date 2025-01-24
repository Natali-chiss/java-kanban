package com.yandex.tasktracker.service.httpTaskManager;

import com.google.gson.*;
import com.yandex.tasktracker.HttpTaskServer;
import com.yandex.tasktracker.model.Epic;
import com.yandex.tasktracker.model.Status;
import com.yandex.tasktracker.model.Subtask;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Обработчик подзадач")
public class HttpTaskManagerSubtasksTest {
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
    @DisplayName("добавляет подзадачу")
    public void testAddSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик", "1");
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest postEpicRequest = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        HttpResponse<String> postEpicResponse = client.send(postEpicRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postEpicResponse.statusCode());
        int epicId = manager.getEpicsList().getFirst().getId();

        Subtask subtask = new Subtask("Подзадача", "1", Status.NEW, epicId, Duration.ofMinutes(5),
                LocalDateTime.of(2025, 2, 25, 18, 0));
        String subtaskJson = gson.toJson(subtask);

        url = URI.create("http://localhost:8080/subtasks");
        HttpRequest postSubtaskRequest = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        HttpResponse<String> postSubtaskResponse = client.send(postSubtaskRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postSubtaskResponse.statusCode());

        List<Subtask> subtasksFromManager = manager.getSubtasksList();
        Subtask createdSubtask = subtasksFromManager.getFirst();

        assertNotNull(subtasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals("Подзадача", createdSubtask.getName(), "Некорректное имя подзадачи");
        assertEquals("1", createdSubtask.getDescription(), "Некорректное описание подзадачи");
        assertEquals(Status.NEW, createdSubtask.getStatus(), "Некорректный статус подзадачи");
        assertEquals("PT5M", createdSubtask.getDuration().toString(),
                "Некорректная длительность подзадачи");
        assertEquals(LocalDateTime.of(2025, 2, 25, 18, 0),
                createdSubtask.getStartTime(), "Некорректное время начала подзадачи");
    }

    @Test
    @DisplayName("обновляет подзадачу")
    public void testUpdateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик", "1");
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest postEpicRequest = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        HttpResponse<String> postEpicResponse = client.send(postEpicRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postEpicResponse.statusCode());
        int epicId = manager.getEpicsList().getFirst().getId();

        Subtask subtask = new Subtask("Подзадача", "1", Status.NEW, epicId, Duration.ofMinutes(5),
                LocalDateTime.of(2025, 2, 25, 18, 0));
        String subtaskJson = gson.toJson(subtask);

        url = URI.create("http://localhost:8080/subtasks");
        HttpRequest postSubtaskRequest = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        HttpResponse<String> postSubtaskResponse = client.send(postSubtaskRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postSubtaskResponse.statusCode());

        int id = manager.getSubtasksList().getFirst().getId();

        Subtask newSubtask = new Subtask("Измененная подзадача", "1.1", Status.NEW, epicId,
                Duration.ofMinutes(10), LocalDateTime.of(2025, 2, 26, 18, 0));
        String newSubtaskJson = gson.toJson(newSubtask);

        url = URI.create("http://localhost:8080/subtasks/" + id);
        HttpRequest updateRequest = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers
                .ofString(newSubtaskJson)).build();

        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, updateResponse.statusCode());

        Subtask updatedSubtask = manager.getSubtask(id);
        assertEquals(newSubtask.getName(), updatedSubtask.getName());
        assertEquals(newSubtask.getDescription(), updatedSubtask.getDescription());
        assertEquals(newSubtask.getStatus(), updatedSubtask.getStatus());
        assertEquals(newSubtask.getDuration(), updatedSubtask.getDuration());
        assertEquals(newSubtask.getStartTime(), updatedSubtask.getStartTime());
    }

    @Test
    @DisplayName("удаляет подзадачу")
    public void testRemoveSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик", "1");
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest postEpicRequest = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        HttpResponse<String> postEpicResponse = client.send(postEpicRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postEpicResponse.statusCode());
        int epicId = manager.getEpicsList().getFirst().getId();

        Subtask subtask = new Subtask("Подзадача", "1", Status.NEW, epicId, Duration.ofMinutes(5),
                LocalDateTime.of(2025, 2, 25, 18, 0));
        String subtaskJson = gson.toJson(subtask);

        url = URI.create("http://localhost:8080/subtasks");
        HttpRequest postSubtaskRequest = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        HttpResponse<String> postSubtaskResponse = client.send(postSubtaskRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postSubtaskResponse.statusCode());

        HttpRequest deleteListRequest = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> deleteListResponse = client.send(deleteListRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteListResponse.statusCode());
        assertEquals(0, manager.getSubtasksList().size(), "Список подзадач не удален");

        HttpRequest postRequest2 = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();
        HttpResponse<String> postResponse2 = client.send(postRequest2, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResponse2.statusCode());
        int id = manager.getSubtasksList().getFirst().getId();

        url = URI.create("http://localhost:8080/subtasks/" + id);
        HttpRequest deleteSubtaskRequest = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> deleteSubtaskResponse = client.send(deleteSubtaskRequest,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteSubtaskResponse.statusCode());
        assertEquals(0, manager.getSubtasksList().size(), "Подзадача не удалена");
    }

    @Test
    @DisplayName("получает подзадачу")
    public void testGetSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик", "1");
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest postEpicRequest = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        HttpResponse<String> postEpicResponse = client.send(postEpicRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postEpicResponse.statusCode());
        int epicId = manager.getEpicsList().getFirst().getId();

        Subtask subtask = new Subtask("Подзадача", "1", Status.NEW, epicId, Duration.ofMinutes(5),
                LocalDateTime.of(2025, 2, 25, 18, 0));
        String subtaskJson = gson.toJson(subtask);

        url = URI.create("http://localhost:8080/subtasks");
        HttpRequest postSubtaskRequest = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        HttpResponse<String> postSubtaskResponse = client.send(postSubtaskRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postSubtaskResponse.statusCode());
        Subtask createdSubtask = manager.getSubtasksList().getFirst();

        HttpRequest getRequest = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());

        JsonElement jsonElement = JsonParser.parseString(getResponse.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        JsonObject subtaskObject = jsonArray.get(0).getAsJsonObject();
        int gottenId = subtaskObject.get("id").getAsInt();
        String name = subtaskObject.get("name").getAsString();
        String description = subtaskObject.get("description").getAsString();
        Status status = Status.valueOf(subtaskObject.get("status").getAsString());
        int gottenEpicId = subtaskObject.get("epicId").getAsInt();
        Duration duration = Duration.parse(subtaskObject.get("duration").getAsString());
        LocalDateTime startTime = LocalDateTime.parse(subtaskObject.get("startTime").getAsString());

        assertEquals(createdSubtask.getId(), gottenId);
        assertEquals(createdSubtask.getName(), name);
        assertEquals(createdSubtask.getDescription(), description);
        assertEquals(createdSubtask.getStatus(), status);
        assertEquals(createdSubtask.getEpicId(), gottenEpicId);
        assertEquals(createdSubtask.getStartTime(), startTime);
        assertEquals(createdSubtask.getDuration(), duration);

        url = URI.create("http://localhost:8080/subtasks/" + createdSubtask.getId());
        HttpRequest getSubtaskRequest = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> getSubtaskResponse = client.send(getSubtaskRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getSubtaskResponse.statusCode());

        jsonElement = JsonParser.parseString(getSubtaskResponse.body());
        subtaskObject = jsonElement.getAsJsonObject();
        gottenId = subtaskObject.get("id").getAsInt();
        name = subtaskObject.get("name").getAsString();
        description = subtaskObject.get("description").getAsString();
        status = Status.valueOf(subtaskObject.get("status").getAsString());
        gottenEpicId = subtaskObject.get("epicId").getAsInt();
        duration = Duration.parse(subtaskObject.get("duration").getAsString());
        startTime = LocalDateTime.parse(subtaskObject.get("startTime").getAsString());

        assertEquals(createdSubtask.getId(), gottenId);
        assertEquals(createdSubtask.getName(), name);
        assertEquals(createdSubtask.getDescription(), description);
        assertEquals(createdSubtask.getStatus(), status);
        assertEquals(createdSubtask.getEpicId(), gottenEpicId);
        assertEquals(createdSubtask.getStartTime(), startTime);
        assertEquals(createdSubtask.getDuration(), duration);
    }
}