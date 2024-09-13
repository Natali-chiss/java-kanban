package com.yandex.tasktracker.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Эпик")
class EpicTest {

    @Test
    @DisplayName("равен другому эпику, если у них одинаковый id")
    void shouldAssertEqualsOfEpicsById() {
        Epic epic1 = new Epic("epic", "1"); // id = 0
        Epic epic2 = new Epic("эпик", "2"); // id = 0
        assertEquals(epic1, epic2);
    }
}