package com.yandex.tasktracker;

import com.yandex.tasktracker.model.Epic;
import com.yandex.tasktracker.model.Status;
import com.yandex.tasktracker.model.Subtask;
import com.yandex.tasktracker.model.Task;
import com.yandex.tasktracker.service.Managers;
import com.yandex.tasktracker.service.TaskManager;

public class Main {

    public static void main(String[] args) {

        TaskManager manager = Managers.getDefault();

        Task task1 = manager.createTask(new Task("Задача", "1", Status.NEW));
        Task task2 = manager.createTask(new Task("Задача", "2", Status.NEW));
        Task taskFromTm = manager.getTask(task1.getId());
        taskFromTm.setStatus(Status.DONE);
        manager.updateTask(taskFromTm);
        task1.setStatus(Status.DONE);
        manager.updateTask(task1);

        Epic epic1 = manager.createEpic(new Epic("epic", "1"));
        Subtask subtask1 = manager.createSubtask(new Subtask("subtask", "1", Status.NEW, epic1.getId()));
        Subtask subtask2 = manager.createSubtask(new Subtask("subtask", "2", Status.NEW, epic1.getId()));
        Epic epicFromTm = manager.getEpic(epic1.getId());
        Subtask subtaskFromTm = manager.getSubtask(subtask2.getId());
        Task ghostTask = manager.getTask(10);
        printAllTasks(manager);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getTasksList()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : manager.getEpicsList()) {
            System.out.println(epic);

            for (Task task : manager.getEpicSubtasks(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getSubtasksList()) {
            System.out.println(subtask);
        }

        System.out.println("История просмотра:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}
