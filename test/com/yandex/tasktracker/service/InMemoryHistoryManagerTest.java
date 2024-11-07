package com.yandex.tasktracker.service;

import com.yandex.tasktracker.model.Epic;
import com.yandex.tasktracker.model.Status;
import com.yandex.tasktracker.model.Subtask;
import com.yandex.tasktracker.model.Task;
import com.yandex.tasktracker.service.history.InMemoryHistoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Менеджер истории")
class InMemoryHistoryManagerTest {

    private InMemoryHistoryManager historyManager;
    Task task1 = new Task("task", "1", Status.NEW);
    Task task2 = new Task("task", "3", Status.NEW, Duration.ofMinutes(40), LocalDateTime.now());
    Epic epic = new Epic("epic", "4");
    Subtask subtask = new Subtask("subtask", "5", Status.NEW, epic.getId(), Duration.ofHours(12),
            LocalDateTime.of(2023, 12, 31, 0, 0));

    @BeforeEach
    void beforeEach() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    @DisplayName("хранит задачи в истории в порядке их добавления")
    void shouldKeepTheCorrectTasksOrder() {
        List<Task> tasksList = new ArrayList<>(List.of(task2, epic, task1, subtask));
        for (Task task : tasksList) {
            historyManager.addTaskToHistory(task);
        }
        List<Task> historyList = historyManager.getTasks();
        for (int i = 0; i < historyList.size(); i++) {
            assertEquals(historyList.get(i), tasksList.get(i));
        }
    }

    @Test
    @DisplayName("добавляет задачи в историю")
    void shouldAddTaskToHistory() {
        historyManager.addTaskToHistory(task1);
        assertEquals(1, historyManager.getTasks().size(), "История пустая");
    }

    @Test
    @DisplayName("добавляет задачи в историю")
    void shouldRemoveTaskFromHistory() {
        historyManager.addTaskToHistory(task1);
        historyManager.remove(task1.getId());
        assertEquals(0, historyManager.getTasks().size());
    }

    @Test
    @DisplayName("избавляется от повторных просмотров в истории.")
    void shouldRemovePreviousWatching() {
        historyManager.addTaskToHistory(task1);
        historyManager.addTaskToHistory(task1);
        assertEquals(1, historyManager.getTasks().size());
    }

    @Test
    @DisplayName("сохраняет предыдущую версию добавленной задачи и её данных")
    void shouldSaveTheLastSaving() {
        historyManager.addTaskToHistory(task1);
        task1.setName("modifiedTask");
        task1.setDescription("1.1");
        task1.setStatus(Status.DONE);
        List<Task> history = historyManager.getTasks();
        assertEquals("task", history.getFirst().getName());
        assertEquals("1", history.getFirst().getDescription());
        assertEquals(Status.NEW, history.getFirst().getStatus());
    }
}