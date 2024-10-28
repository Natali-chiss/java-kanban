package com.yandex.tasktracker.service;

import com.yandex.tasktracker.model.Epic;
import com.yandex.tasktracker.model.Status;
import com.yandex.tasktracker.model.Task;
import com.yandex.tasktracker.service.history.InMemoryHistoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

@DisplayName("Менеджер задач")
class InMemoryTaskManagerTest<T extends TaskManager> extends TaskManagerTest<T> {

    @Override
    @BeforeEach
    void shouldInit() {
        taskManager = (T) new InMemoryTaskManager(new InMemoryHistoryManager());
        task = new Task("task", "1", Status.DONE);
        epic = new Epic("epic", "1");
    }
}