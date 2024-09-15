package com.yandex.tasktracker.service;

import com.yandex.tasktracker.model.Epic;
import com.yandex.tasktracker.model.Status;
import com.yandex.tasktracker.model.Subtask;
import com.yandex.tasktracker.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Менеджер истории")
class InMemoryHistoryManagerTest {

    private InMemoryHistoryManager historyManager;
    private List<Task> history;
    Task task1 = new Task("task", "1", Status.NEW);
    Task task2 = new Task("task", "2", Status.NEW);
    Task task3 = new Task("task", "3", Status.NEW);
    Epic epic = new Epic("epic", "4");
    Subtask subtask = new Subtask("subtask", "5", Status.NEW, epic.getId());

    @BeforeEach
    void beforeEach() {
        historyManager = new InMemoryHistoryManager();
        history = historyManager.getHistory();
    }

    @Test
    @DisplayName("добавляет задачи в историю")
    void shouldAddTaskToHistory() {
        historyManager.addTaskToHistory(task1);
        assertNotNull(history, "История пустая");
    }

    @Test
    @DisplayName("хранит 10 последних просмотренных задач")
    void shouldRemoveFirstTaskIfAdd11Tasks() {
        for (int i = 1; i <= 2; i++) {
            historyManager.addTaskToHistory(task1);
            historyManager.addTaskToHistory(task2);
            historyManager.addTaskToHistory(task3);
            historyManager.addTaskToHistory(epic);
            historyManager.addTaskToHistory(subtask);
        }
        assertEquals(10, history.size());
        historyManager.addTaskToHistory(task1);
        assertEquals(10, history.size());
        assertEquals(history.getFirst(), task2);
    }

    @Test
    @DisplayName("сохраняет предыдущую версию добавленной задачи и её данных")
    void shouldSaveTheLastSaving() {
        historyManager.addTaskToHistory(task1);
        task1.setName("modifiedTask");
        task1.setDescription("1.1");
        task1.setStatus(Status.DONE);
        historyManager.addTaskToHistory(task1);
        assertEquals("task", history.getFirst().getName());
        assertEquals("modifiedTask", history.get(1).getName());
        assertEquals("1", history.getFirst().getDescription());
        assertEquals("1.1", history.get(1).getDescription());
        assertEquals(Status.NEW, history.getFirst().getStatus());
        assertEquals(Status.DONE, history.get(1).getStatus());
    }
}