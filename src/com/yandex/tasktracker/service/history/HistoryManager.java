package com.yandex.tasktracker.service.history;

import com.yandex.tasktracker.model.Task;

import java.util.List;

public interface HistoryManager {

    void addTaskToHistory(Task task);

    void remove(int id);

    List<Task> getTasks();
}