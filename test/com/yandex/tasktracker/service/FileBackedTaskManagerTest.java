package com.yandex.tasktracker.service;

import com.yandex.tasktracker.model.Epic;
import com.yandex.tasktracker.model.Status;
import com.yandex.tasktracker.model.Subtask;
import com.yandex.tasktracker.model.Task;
import com.yandex.tasktracker.service.file.FileBackedTaskManager;
import com.yandex.tasktracker.service.history.InMemoryHistoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;

@DisplayName("Файловый менеджер")
public class FileBackedTaskManagerTest<T extends TaskManager> extends TaskManagerTest<T> {

    Path tempFile;

    @Override
    @BeforeEach
    void shouldInit() {
        try {
            tempFile = Files.createTempFile("tasksFile-", ".txt");
            taskManager = (T) new FileBackedTaskManager(tempFile, new InMemoryHistoryManager());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        task = new Task("task", "1", Status.DONE);
        epic = new Epic("epic", "1");
    }

    @Test
    @DisplayName("восстанавливает менеджер из файла")
    void shouldLoadFromFile() {
        Task task1 = taskManager.createTask(task);
        Epic epic1 = taskManager.createEpic(epic);
        Subtask subtask1 = taskManager.createSubtask
                (new Subtask("subtask", "1", Status.NEW, epic1.getId()));
        Subtask subtask2 = taskManager.createSubtask
                (new Subtask("subtask", "2", Status.IN_PROGRESS, epic1.getId()));
        assertEquals(Status.IN_PROGRESS, epic1.getStatus());

        FileBackedTaskManager newManager = new FileBackedTaskManager(tempFile);
        newManager.init();
        assertEquals(0, newManager.getHistory().size());

        Task loadedTask = newManager.getTask(task1.getId());
        assertEquals(task1.getName(), loadedTask.getName());
        assertEquals(task1.getDescription(), loadedTask.getDescription());
        assertEquals(task1.getStatus(), loadedTask.getStatus());

        Epic loadedEpic = newManager.getEpic(epic1.getId());
        assertEquals(epic1.getName(), loadedEpic.getName());
        assertEquals(epic1.getDescription(), loadedEpic.getDescription());
        assertEquals(epic1.getStatus(), loadedEpic.getStatus());
        assertEquals(epic1.getId(), loadedEpic.getId());
        assertEquals(2, epic1.getSubtasksIds().size());

        Subtask loadedSubtask1 = newManager.getSubtask(subtask1.getId());
        assertEquals(subtask1.getName(), loadedSubtask1.getName());
        assertEquals(subtask1.getDescription(), loadedSubtask1.getDescription());
        assertEquals(subtask1.getStatus(), loadedSubtask1.getStatus());
        assertEquals(subtask1.getEpicId(), loadedSubtask1.getEpicId());

        Subtask loadedSubtask2 = newManager.getSubtask(subtask2.getId());
        assertEquals(subtask2.getName(), loadedSubtask2.getName());
        assertEquals(subtask2.getDescription(), loadedSubtask2.getDescription());
        assertEquals(subtask2.getStatus(), loadedSubtask2.getStatus());
        assertEquals(subtask2.getEpicId(), loadedSubtask2.getEpicId());

        assertEquals(4, newManager.getHistory().size());
    }
}
