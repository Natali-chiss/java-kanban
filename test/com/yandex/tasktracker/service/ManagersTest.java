package com.yandex.tasktracker.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Менеджеры")
class ManagersTest {

    @Test
    @DisplayName("возвращает проинициализированный менеджер задач")
    void shouldReturnInitializedTaskManager() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager);
    }

    @Test
    @DisplayName("возвращает проинициализированный менеджер истории")
    void shouldReturnInitializedHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager);
    }
}