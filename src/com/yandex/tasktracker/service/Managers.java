package com.yandex.tasktracker.service;

import com.yandex.tasktracker.service.history.HistoryManager;
import com.yandex.tasktracker.service.history.InMemoryHistoryManager;

public class Managers {

    private Managers() {
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
