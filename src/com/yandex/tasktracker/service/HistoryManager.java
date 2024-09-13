package com.yandex.tasktracker.service;

import com.yandex.tasktracker.model.Task;
import java.util.ArrayList;

public interface HistoryManager {

    void addTaskToHistory(Task task);

    ArrayList<Task> getHistory();
}
