package com.yandex.tasktracker.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Подзадача")
class SubtaskTest {

    @Test
    @DisplayName("равна другой подзадаче, если у них одинаковый id")
    void shouldAssertEqualsOfSubtasksById() {
        Epic epic = new Epic("epic", "1");
        Subtask subtask1 = new Subtask("subtask", "1", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("подзадача", "2", Status.IN_PROGRESS, epic.getId());
        assertEquals(subtask1, subtask2);
    }
}