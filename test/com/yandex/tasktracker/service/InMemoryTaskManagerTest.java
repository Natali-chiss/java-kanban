package com.yandex.tasktracker.service;

import com.yandex.tasktracker.model.Epic;
import com.yandex.tasktracker.model.Status;
import com.yandex.tasktracker.model.Subtask;
import com.yandex.tasktracker.model.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Менеджер задач")
class InMemoryTaskManagerTest {

    final InMemoryTaskManager taskManager = new InMemoryTaskManager(new InMemoryHistoryManager());
    Task task = new Task("task", "1", Status.DONE);
    Epic epic = new Epic("epic", "1");

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
    }

    @Test
    @DisplayName("создаёт эпики и находит их по id")
    void shouldCreateEpicAndGetById() {
        Epic epic1 = taskManager.createEpic(epic);
        assertNotNull(epic1, "Эпик не создан.");
        Epic savedEpic = taskManager.getEpic(epic1.getId());
        assertNotNull(savedEpic, "Эпик не найден.");
    }

    @Test
    @DisplayName("создаёт подзадачи и находит их по id")
    void shouldCreateSubtaskAndGetById() {
        Epic epic1 = taskManager.createEpic(epic);
        Subtask subtask1 = taskManager.createSubtask
                (new Subtask("subtask", "1", Status.NEW, epic1.getId()));
        assertNotNull(subtask1, "Подзадача не создана.");
        Subtask savedSubtask = taskManager.getSubtask(subtask1.getId());
        assertNotNull(savedSubtask, "Подзадача не найдена.");
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