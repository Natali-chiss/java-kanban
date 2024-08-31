package com.yandex.tasktracker.service;

import com.yandex.tasktracker.model.Epic;
import com.yandex.tasktracker.model.Status;
import com.yandex.tasktracker.model.Subtask;
import com.yandex.tasktracker.model.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {

    public TaskManager() {
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
    }

    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, Subtask> subtasks;

    private int newId = 0;

    public Task createTask(Task task) {
        task.setId(++newId);
        tasks.put(newId, task);
        return task;
    }

    public Epic createEpic(Epic epic) {
        epic.setId(++newId);
        epic.removeAllSubtasks();
        epics.put(newId, epic);
        return epic;
    }

    public Subtask createSubtask(Subtask subtask) {
        subtask.setId(++newId);
        subtasks.put(newId, subtask);
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtask(subtask.getId());
        calculateStatus(epic);
        return subtask;
    }

    public void updateTask(Task task) {
        Task saved = tasks.get(task.getId());
        if (saved == null) {
            return;
        }
        saved.setName(task.getName());
        saved.setDescription(task.getDescription());
        saved.setStatus(task.getStatus());
    }

    public void updateEpic(Epic epic) {
        Epic saved = epics.get(epic.getId());
        if (saved == null) {
            return;
        }
        saved.setName(epic.getName());
        saved.setDescription(epic.getDescription());
    }

    public void updateSubtask(Subtask subtask) {
        Subtask saved = subtasks.get(subtask.getId());
        if (saved == null) {
            return;
        }
        saved.setName(subtask.getName());
        saved.setDescription(subtask.getDescription());
        saved.setStatus(subtask.getStatus());
        calculateStatus(epics.get(subtask.getEpicId()));
    }

    public void clearTasksList() {
        tasks.clear();
    }

    public void clearEpicsList() {
        epics.clear();
    }

    public void clearSubtasksList() {
        subtasks.clear();
    }

    public Task getTask(int id) {
        return tasks.get(id);
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    public ArrayList<Task> getTasksList() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getEpicsList() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getSubtasksList() {
        return new ArrayList<>(subtasks.values());
    }

    public void removeTask(int id) {
        tasks.remove(id);
    }

    void removeEpic(int id) {
        Epic epic = epics.get(id);
        for (Integer subtaskId : epic.getSubtasksIds()) {
            subtasks.remove(subtaskId);
        }
        epics.remove(id);
    }

    public void removeSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        Epic epic = epics.get(subtask.getEpicId());
        epic.removeSubtask(id);
        calculateStatus(epic);
    }

    public ArrayList<Subtask> getEpicSubtasks(int id) {
        ArrayList<Subtask> epicSubtasks = new ArrayList<>();
        for (Integer subtaskId : epics.get(id).getSubtasksIds()) {
            epicSubtasks.add(subtasks.get(subtaskId));
        }
        return epicSubtasks;
    }

    private void calculateStatus(Epic epic) {
        Status status = Status.NEW;
        int numberD = 0;
        int numberN = 0;
        if (!epic.getSubtasksIds().isEmpty()) {
            for (Integer id : epic.getSubtasksIds()) {
                Subtask subtask = subtasks.get(id);
                if (subtask != null) {
                    if (subtask.getStatus() == Status.DONE) {
                        numberD++;
                    } else if (subtask.getStatus() == Status.NEW) {
                        numberN++;
                    } else {
                        epic.setStatus(Status.IN_PROGRESS);
                        return;
                    }
                }
            }
            if (numberD == epic.getSubtasksIds().size()) {
                status = Status.DONE;
            } else if (numberN == epic.getSubtasksIds().size()) {
                return;
            } else {
                status = Status.IN_PROGRESS;
            }
        }
        epic.setStatus(status);
    }
}
