package com.yandex.tasktracker.service;

import com.yandex.tasktracker.model.Epic;
import com.yandex.tasktracker.model.Status;
import com.yandex.tasktracker.model.Subtask;
import com.yandex.tasktracker.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;
    Task task;
    Epic epic;

    @BeforeEach
    abstract void shouldInit();

    @Test
    @DisplayName("проверяет, что задачи не пересекаются по времени")
    void shouldThrowsValidationException() {
        assertThrows(ValidationException.class, () -> {
            Epic epic1 = taskManager.createEpic(epic);
            Subtask subtask1 = taskManager.createSubtask
                    (new Subtask("subtask", "1", Status.DONE, epic1.getId(), Duration.ofMinutes(60),
                            LocalDateTime.now()));
            Task crossedTask = taskManager.createTask(task);
        }, "Пересечение задач по времени должно приводить к исключению");
    }

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
    @DisplayName("проверяет логику обновления эпика")
    void shouldReturnCorrectEpicStatus() {
        Epic epic1 = taskManager.createEpic(epic);
        Epic savedEpic = taskManager.getEpic(epic1.getId());
        assertEquals(Status.NEW, epic1.getStatus());

        Subtask subtask1 = taskManager.createSubtask
                (new Subtask("subtask", "1", Status.DONE, epic1.getId(), Duration.ofMinutes(60),
                        LocalDateTime.now()));
        Subtask savedSubtask1 = taskManager.getSubtask(subtask1.getId());
        assertEquals(Status.DONE, epic1.getStatus());
        assertEquals(subtask1.getStartTime(), epic1.getStartTime());
        assertEquals(subtask1.getDuration(), epic1.getDuration());
        assertEquals(subtask1.getEndTime(), epic1.getEndTime());

        Subtask subtask2 = taskManager.createSubtask
                (new Subtask("subtask", "2", Status.NEW, epic1.getId(), Duration.ofHours(24),
                        LocalDateTime.of(2024, 3, 11, 0, 0)));
        Subtask savedSubtask2 = taskManager.getSubtask(subtask2.getId());
        assertEquals(3, taskManager.getHistory().size());
        assertEquals(Status.IN_PROGRESS, epic1.getStatus());
        assertEquals(subtask2.getStartTime(), epic1.getStartTime());
        assertEquals(subtask1.getDuration().plus(subtask2.getDuration()), epic1.getDuration());
        assertEquals(subtask1.getEndTime(), epic1.getEndTime());

        taskManager.removeSubtask(subtask1.getId());
        assertEquals(Status.NEW, epic1.getStatus());
        assertEquals(subtask2.getStartTime(), epic1.getStartTime());
        assertEquals(subtask2.getDuration(), epic1.getDuration());
        assertEquals(subtask2.getEndTime(), epic1.getEndTime());

        subtask2.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(subtask2);
        assertEquals(Status.IN_PROGRESS, epic1.getStatus());

        taskManager.removeSubtask(subtask2.getId());
        assertEquals(Status.NEW, epic1.getStatus());
        assertEquals(1, taskManager.getHistory().size());
        assertNull(epic1.getStartTime());
        assertEquals(Duration.ofMinutes(0), epic1.getDuration());
        assertNull(epic1.getEndTime());
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
        assertThrows(NullPointerException.class, () -> {
            Subtask subtask2 = taskManager.createSubtask(new Subtask
                    (subtask1.getName(), subtask1.getDescription(), subtask1.getStatus(), subtask1.getId()));
        }, "Создание подзадачи с эпиком, который не эпик, должно приводить к исключению");
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
        assertEquals(task.getStartTime(), task1.getStartTime());
        assertEquals(task.getDuration(), task1.getDuration());
    }
}
