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
        System.out.println("Проверка задач:");
        System.out.println("Индексы задач совпадают: " + (task1.getId() == loadedTask.getId()) + " - "
                + loadedTask.getId());
        System.out.println("Имена задач совпадают: " + task1.getName().equals(loadedTask.getName()) + " - "
                + loadedTask.getName());
        System.out.println("Описания задач совпадают: " + task1.getDescription().equals(loadedTask.getDescription())
                + " - " + loadedTask.getDescription());
        System.out.println("Статус задач совпадает: " + task1.getStatus().equals(loadedTask.getStatus()) + " - "
                + loadedTask.getStatus());
        System.out.println("Время начала задач совпадает: " + task1.getStartTime().equals(loadedTask.getStartTime())
                + " - " + loadedTask.getStartTime());
        System.out.println("Длительность задач совпадает: " + task1.getDuration().equals(loadedTask.getDuration())
                + " - " + loadedTask.getDuration());

        Epic loadedEpic = loadFromFile.getEpic(epic1.getId());
        System.out.println("\nПроверка эпиков");
        System.out.println("Индексы эпиков совпадают: " + (epic1.getId() == loadedEpic.getId()) + " - "
                + loadedEpic.getId());
        System.out.println("Имена эпиков совпадают: " + epic1.getName().equals(loadedEpic.getName()) + " - "
                + loadedEpic.getName());
        System.out.println("Описания эпиков совпадают: " + epic1.getDescription().equals(loadedEpic.getDescription())
                + " - " + loadedEpic.getDescription());
        System.out.println("Статус эпиков совпадает: " + epic1.getStatus().equals(loadedEpic.getStatus()) + " - "
                + loadedEpic.getStatus());
        System.out.println("Время начала эпиков совпадает: " + epic1.getStartTime().equals(loadedEpic.getStartTime())
                + " - " + loadedEpic.getStartTime());
        System.out.println("Длительность эпиков совпадает: " + epic1.getDuration().equals(loadedEpic.getDuration())
                + " - " + loadedEpic.getDuration());
        System.out.println("Количество подзадач эпиков совпадает: " + (epic1.getSubtasksIds().size()
                == loadedEpic.getSubtasksIds().size()) + " - " + loadedEpic.getSubtasksIds().size());

        for (Subtask subtask : fileBackedTaskManager.subtasks.values()) {
            Subtask loadedSubtask = loadFromFile.getSubtask(subtask.getId());
            System.out.println("\nПроверка подзадач");
            System.out.println("Индексы подзадач совпадают: " + (subtask.getId() == loadedSubtask.getId()) + " - "
                    + loadedSubtask.getId());
            System.out.println("Имена подзадач совпадают: " + subtask.getName().equals(loadedSubtask.getName()) + " - "
                    + loadedSubtask.getName());
            System.out.println("Описания подзадач совпадают: "
                    + subtask.getDescription().equals(loadedSubtask.getDescription()) + " - "
                    + loadedSubtask.getDescription());
            System.out.println("Статус подзадач совпадает: " + subtask.getStatus().equals(loadedSubtask.getStatus())
                    + " - " + loadedSubtask.getStatus());
            System.out.println("Id эпика подзадач совпадают: " + subtask.getEpicId().equals(loadedSubtask.getEpicId())
                    + " - " + loadedSubtask.getEpicId());
            if (subtask.getStartTime() != null) {
                System.out.println("Время начала подзадач совпадает: "
                        + subtask.getStartTime().isEqual(loadedSubtask.getStartTime()) + " - "
                        + loadedSubtask.getStartTime());
            } else {
                if (loadedSubtask.getStartTime() == null) {
                    System.out.println("Время начала подзадач совпадает: true - null");
                } else {
                    System.out.println("Время начала подзадач не совпадает: " + loadedSubtask.getStartTime());
                }
            }
            System.out.println("Длительность подзадач совпадает: "
                    + subtask.getDuration().equals(loadedSubtask.getDuration()) + " - " + loadedSubtask.getDuration());
        }

        assertEquals(4, loadFromFile.getHistory().size());

        Task newTask = loadFromFile.createTask(new Task("task", "2", Status.NEW));
        System.out.println("Id созданной задачи больше существующих id: " + (newTask.getId() > subtask2.getId()) + " - "
                + newTask.getId());
    }
}