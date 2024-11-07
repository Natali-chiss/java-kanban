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
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;

@DisplayName("Файловый менеджер")
public class FileBackedTaskManagerTest<T extends TaskManager> extends TaskManagerTest<T> {

    Path tempFile;
    FileBackedTaskManager fileBackedTaskManager;

    @Override
    @BeforeEach
    void shouldInit() {
        try {
            tempFile = Files.createTempFile("tasksFile-", ".txt");
            fileBackedTaskManager = new FileBackedTaskManager(tempFile, new InMemoryHistoryManager());
            taskManager = (T) fileBackedTaskManager;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        task = new Task("task", "1", Status.DONE, Duration.ofMinutes(15), LocalDateTime.now());
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
                (new Subtask("subtask", "2", Status.IN_PROGRESS, epic1.getId(), Duration.ofMinutes(10),
                        LocalDateTime.of(2024, 10, 1, 9, 0)));
        assertEquals(Status.IN_PROGRESS, epic1.getStatus());

        FileBackedTaskManager loadFromFile = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(0, loadFromFile.getHistory().size());

        Task loadedTask = loadFromFile.getTask(task1.getId());
        assertThat(loadedTask).usingRecursiveComparison().isEqualTo(task1);

        Epic loadedEpic = loadFromFile.getEpic(epic1.getId());
        assertThat(loadedEpic).usingRecursiveComparison().isEqualTo(epic1);

        for (Subtask subtask : fileBackedTaskManager.subtasks.values()) {
            Subtask loadedSubtask = loadFromFile.getSubtask(subtask.getId());
            assertThat(loadedSubtask).usingRecursiveComparison().isEqualTo(subtask);
        }

        assertEquals(4, loadFromFile.getHistory().size());

        Task newTask = loadFromFile.createTask(new Task("task", "2", Status.NEW));
        assertThat(newTask.getId()).isGreaterThan(subtask2.getId());
    }
}
