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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Обработчик эпиков")
public class HttpTaskManagerEpicsTest {
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
    @DisplayName("добавляет эпик")
    public void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик", "1");
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = manager.getEpicsList();
        Epic createdEpic = epicsFromManager.getFirst();

        assertNotNull(epicsFromManager, "Эпики не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Эпик", createdEpic.getName(), "Некорректное имя эпика");
        assertEquals("1", createdEpic.getDescription(), "Некорректное описание эпика");
        assertEquals(Status.NEW, createdEpic.getStatus(), "Некорректный статус эпика");
    }

    @Test
    @DisplayName("обновляет эпик")
    public void testUpdateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик", "1");
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        int id = manager.getEpicsList().getFirst().getId();

        Epic newEpic = new Epic("Измененный эпик", "1.1");
        epicJson = gson.toJson(newEpic);

        url = URI.create("http://localhost:8080/epics/" + id);
        HttpRequest updateRequest = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers
                .ofString(epicJson)).build();

        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, updateResponse.statusCode());

        Epic updatedEpic = manager.getEpic(id);
        assertEquals(newEpic.getName(), updatedEpic.getName(), "Некорректное имя задачи");
        assertEquals(newEpic.getDescription(), updatedEpic.getDescription(), "Некорректное описание задачи");
    }

    @Test
    @DisplayName("удаляет эпик")
    public void testRemoveEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик", "1");
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        HttpRequest deleteListRequest = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> deleteListResponse = client.send(deleteListRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteListResponse.statusCode());
        assertEquals(0, manager.getEpicsList().size(), "Список эпиков не удален");

        HttpRequest postRequest2 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> postResponse2 = client.send(postRequest2, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResponse2.statusCode());
        int id = manager.getEpicsList().getFirst().getId();

        url = URI.create("http://localhost:8080/epics/" + id);
        HttpRequest deleteEpicRequest = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> deleteEpicResponse = client.send(deleteEpicRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteEpicResponse.statusCode());
        assertEquals(0, manager.getEpicsList().size(), "Эпик не удален");
    }

    @Test
    @DisplayName("получает эпик")
    public void testGetEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик", "1");
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        Epic createdEpic = manager.getEpicsList().getFirst();
        int epicId = createdEpic.getId();

        HttpRequest getRequest = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());

        JsonElement jsonElement = JsonParser.parseString(getResponse.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        JsonObject epicObject = jsonArray.get(0).getAsJsonObject();
        int gottenId = epicObject.get("id").getAsInt();
        String name = epicObject.get("name").getAsString();
        String description = epicObject.get("description").getAsString();
        Status status = Status.valueOf(epicObject.get("status").getAsString());

        assertEquals(epicId, gottenId);
        assertEquals(createdEpic.getName(), name);
        assertEquals(createdEpic.getDescription(), description);
        assertEquals(createdEpic.getStatus(), status);

        url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest getEpicRequest = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> getEpicResponse = client.send(getEpicRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getEpicResponse.statusCode());

        jsonElement = JsonParser.parseString(getEpicResponse.body());
        epicObject = jsonElement.getAsJsonObject();
        gottenId = epicObject.get("id").getAsInt();
        name = epicObject.get("name").getAsString();
        description = epicObject.get("description").getAsString();
        status = Status.valueOf(epicObject.get("status").getAsString());

        assertEquals(epicId, gottenId);
        assertEquals(createdEpic.getName(), name);
        assertEquals(createdEpic.getDescription(), description);
        assertEquals(createdEpic.getStatus(), status);
    }

    @Test
    @DisplayName("получает список подзадач эпика")
    public void testGetEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик", "1");
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        int epicId = manager.getEpicsList().getFirst().getId();

        Subtask subtask = new Subtask("Подзадача", "1", Status.NEW, epicId);
        String subtaskJson = gson.toJson(subtask);

        url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request2 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response2.statusCode());
        int subtaskId = manager.getSubtasksList().getFirst().getId();

        url = URI.create("http://localhost:8080/epics/" + epicId + "/subtasks");
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

        assertEquals(subtaskId, gottenId);
        assertEquals(subtask.getName(), name);
        assertEquals(subtask.getDescription(), description);
        assertEquals(subtask.getStatus(), status);
        assertEquals(subtask.getEpicId(), gottenEpicId);
    }
}
