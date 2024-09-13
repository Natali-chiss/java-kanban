package com.yandex.tasktracker.service;

import com.yandex.tasktracker.model.Task;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    private final ArrayList<Task> historyOfWatching = new ArrayList<>(10);

    @Override
    public void addTaskToHistory(Task task) {
        if (historyOfWatching.size() == 10) {
            historyOfWatching.removeFirst();
        }
        Task savedTask = new Task(task.getName(), task.getDescription(), task.getStatus());
        savedTask.setId(task.getId());
        historyOfWatching.add(savedTask);
    }

    @Override
    public ArrayList<Task> getHistory() {
        return historyOfWatching;
    }
}
