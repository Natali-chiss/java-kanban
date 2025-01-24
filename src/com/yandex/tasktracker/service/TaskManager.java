package com.yandex.tasktracker.service;

import com.yandex.tasktracker.model.Epic;
import com.yandex.tasktracker.model.Subtask;
import com.yandex.tasktracker.model.Task;

import java.util.List;

public interface TaskManager {

    Task createTask(Task task);

    Epic createEpic(Epic epic);

    Subtask createSubtask(Subtask subtask);

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    void clearTasksList();

    void clearEpicsList();

    void clearSubtasksList();

    Task getTask(Integer id);

    Epic getEpic(Integer id);

    Subtask getSubtask(Integer id);

    List<Task> getTasksList();

    List<Epic> getEpicsList();

    List<Subtask> getSubtasksList();

    void removeTask(Integer id);

    void removeEpic(Integer id);

    void removeSubtask(Integer id);

    List<Subtask> getEpicSubtasks(Integer id);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}