package com.yandex.tasktracker.service;

import com.yandex.tasktracker.model.Epic;
import com.yandex.tasktracker.model.Status;
import com.yandex.tasktracker.model.Subtask;
import com.yandex.tasktracker.model.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Менеджер задач")
class InMemoryTaskManagerTest {

    final InMemoryTaskManager taskManager = new InMemoryTaskManager(new InMemoryHistoryManager());
    Task task = new Task("task", "1", Status.DONE);
    Epic epic = new Epic("epic", "1");

    @Test
    @DisplayName("проверяет, что внутри эпиков не остаётся неактуальных id подзадач")
    void shouldReturnCorrectEpicSubtasksIds() {
        Epic epic1 = taskManager.createEpic(epic);
        assertEquals(0, epic1.getSubtasksIds().size());
        Epic savedEpic = taskManager.getEpic(epic1.getId());
        assertEquals(1, taskManager.getHistory().size());
        Subtask subtask1 = taskManager.createSubtask
                (new Subtask("subtask", "1", Status.DONE, epic1.getId()));
        Subtask savedSubtask = taskManager.getSubtask(subtask1.getId());
        assertEquals(2, taskManager.getHistory().size());
        assertEquals(1, epic1.getSubtasksIds().size());
        assertEquals(subtask1.getId(), epic1.getSubtasksIds().getFirst());
        taskManager.removeSubtask(subtask1.getId());
        assertEquals(0, epic1.getSubtasksIds().size());
        assertEquals(1, taskManager.getHistory().size());
    }

    @Test
    @DisplayName("проверяет логику обновления статуса эпика")
    void shouldReturnCorrectEpicStatus() {
        Epic epic1 = taskManager.createEpic(epic);
        Epic savedEpic = taskManager.getEpic(epic1.getId());
        assertEquals(Status.NEW, epic1.getStatus());
        Subtask subtask1 = taskManager.createSubtask
                (new Subtask("subtask", "1", Status.DONE, epic1.getId()));
        Subtask savedSubtask1 = taskManager.getSubtask(subtask1.getId());
        assertEquals(Status.DONE, epic1.getStatus());
        Subtask subtask2 = taskManager.createSubtask
                (new Subtask("subtask", "2", Status.NEW, epic1.getId()));
        Subtask savedSubtask2 = taskManager.getSubtask(subtask2.getId());
        assertEquals(3, taskManager.getHistory().size());
        assertEquals(Status.IN_PROGRESS, epic1.getStatus());
        taskManager.removeSubtask(subtask1.getId());
        taskManager.removeSubtask(subtask2.getId());
        assertEquals(Status.NEW, epic1.getStatus());
        assertEquals(1, taskManager.getHistory().size());
    }

    @Test
    @DisplayName("нельзя добавить эпик в самого себя в виде подзадачи")
    void shouldNotAddEpicInSubtask() {
        Epic epic1 = taskManager.createEpic(epic);
        epic1.addSubtask(epic1.getId());
        boolean isHavingSubtask = !taskManager.getEpicSubtasks(epic1.getId()).isEmpty();
        assertFalse(isHavingSubtask);
    }

    @Test
    @DisplayName("подзадачу нельзя сделать своим же эпиком")
    void shouldNotMakeSubtaskItsEpic() {
        Epic epic1 = taskManager.createEpic(epic);
        Subtask subtask1 = taskManager.createSubtask
                (new Subtask("subtask", "1", Status.NEW, epic1.getId()));
        Subtask subtask2 = new Subtask
                (subtask1.getName(), subtask1.getDescription(), subtask1.getStatus(), subtask1.getId());
        subtask1.setId(subtask2.getId());
        taskManager.updateSubtask(subtask1);
        assertEquals(subtask1.getEpicId(), epic1.getId());
    }

    @Test
    @DisplayName("создаёт задачи и находит их по id")
    void shouldCreateTaskAndGetById() {
        Task task1 = taskManager.createTask(task);
        assertNotNull(task1, "Задача не создана.");
        Task savedTask = taskManager.getTask(task1.getId());
        assertNotNull(savedTask, "Задача не найдена.");
        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size());
    }

    @Test
    @DisplayName("создаёт эпики и находит их по id")
    void shouldCreateEpicAndGetById() {
        Epic epic1 = taskManager.createEpic(epic);
        assertNotNull(epic1, "Эпик не создан.");
        Epic savedEpic = taskManager.getEpic(epic1.getId());
        assertNotNull(savedEpic, "Эпик не найден.");
        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size());
    }

    @Test
    @DisplayName("создаёт подзадачи и находит их по id")
    void shouldCreateSubtaskAndGetById() {
        Epic epic1 = taskManager.createEpic(epic);
        Epic savedEpic = taskManager.getEpic(epic1.getId());
        Subtask subtask1 = taskManager.createSubtask
                (new Subtask("subtask", "1", Status.NEW, epic1.getId()));
        assertNotNull(subtask1, "Подзадача не создана.");
        Subtask savedSubtask = taskManager.getSubtask(subtask1.getId());
        assertEquals(2, taskManager.getHistory().size());
        assertNotNull(savedSubtask, "Подзадача не найдена.");
        taskManager.removeEpic(epic1.getId());
        assertEquals(0, taskManager.getHistory().size());
    }

    @Test
    @DisplayName("проверяет неизменность задачи при добавлении в менеджер")
    void shouldAddTheSameTask() {
        Task task1 = taskManager.createTask(task);
        assertEquals(task.getName(), task1.getName());
        assertEquals(task.getDescription(), task1.getDescription());
        assertEquals(task.getStatus(), task1.getStatus());
        assertEquals(task.getId(), task1.getId());
    }
}