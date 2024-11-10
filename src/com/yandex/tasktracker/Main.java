package com.yandex.tasktracker;

import com.yandex.tasktracker.model.Epic;
import com.yandex.tasktracker.model.Status;
import com.yandex.tasktracker.model.Subtask;
import com.yandex.tasktracker.model.Task;
import com.yandex.tasktracker.service.Managers;
import com.yandex.tasktracker.service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {

        TaskManager manager = Managers.getDefault();

        Task task1 = manager.createTask(new Task("Задача", "1", Status.NEW));
        Task task2 = manager.createTask(new Task("Задача", "2", Status.NEW, Duration.ofMinutes(60),
                LocalDateTime.now()));
        Task taskFromTm = manager.getTask(task1.getId());
        taskFromTm.setStatus(Status.DONE);
        manager.updateTask(taskFromTm);
        task1.setStatus(Status.DONE);
        manager.updateTask(task1);

        Epic epic1 = manager.createEpic(new Epic("epic", "1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("subtask", "1", Status.NEW, epic1.getId(),
                Duration.ofHours(2), LocalDateTime.of(2024, 12, 15, 18, 0)));
        Subtask subtask2 = manager.createSubtask(new Subtask("subtask", "2", Status.NEW, epic1.getId()));
        Epic epicFromTm = manager.getEpic(epic1.getId());
        Subtask subtaskFromTm = manager.getSubtask(subtask1.getId());
        printAllTasks(manager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getTasksList()) {
            System.out.println(task);
        }
        System.out.println("\nЭпики:");
        for (Task epic : manager.getEpicsList()) {
            System.out.println(epic);

            for (Task task : manager.getEpicSubtasks(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("\nПодзадачи:");
        for (Task subtask : manager.getSubtasksList()) {
            System.out.println(subtask);
        }

        System.out.println("\nИстория просмотра:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }

        System.out.println("\nСписок задач по времени начала:");
        for (Task task : manager.getPrioritizedTasks()) {
            System.out.println(task);
        }
    }
}