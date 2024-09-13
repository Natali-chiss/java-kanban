package com.yandex.tasktracker.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Задача")
class TaskTest {

    @Test
    @DisplayName("равна другой задаче, если у них одинаковый id")
    void shouldAssertEqualsOfTasksById() {
        Task task1 = new Task("task", "1", Status.NEW); // id = 0
        Task task2 = new Task("задача", "2", Status.IN_PROGRESS); // id = 0
        assertEquals(task1, task2);
    }
}