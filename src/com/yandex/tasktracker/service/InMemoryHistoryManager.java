package com.yandex.tasktracker.service;

import com.yandex.tasktracker.model.Task;

import java.util.List;
import java.util.LinkedList;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> historyOfWatching = new LinkedList<>();
    private static final int MAX_SIZE_OF_HISTORY = 10;

    @Override
    public void addTaskToHistory(Task task) {
        if (task == null) {
            return;
        }
        if (historyOfWatching.size() == MAX_SIZE_OF_HISTORY) {
            historyOfWatching.removeFirst();
        }
        Task savedTask = new Task(task.getName(), task.getDescription(), task.getStatus());
        savedTask.setId(task.getId());
        historyOfWatching.add(savedTask);
    }

    @Override
    public List<Task> getHistory() {
        return historyOfWatching;
    }
}
