package com.yandex.tasktracker.service;

import com.yandex.tasktracker.model.Epic;
import com.yandex.tasktracker.model.Status;
import com.yandex.tasktracker.model.Subtask;
import com.yandex.tasktracker.model.Task;
import com.yandex.tasktracker.service.history.HistoryManager;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {

    protected final Map<Integer, Task> tasks;
    protected final Map<Integer, Epic> epics;
    protected final Map<Integer, Subtask> subtasks;
    private final HistoryManager historyManager;

    protected int newId = 0;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
    }

    @Override
    public Task createTask(Task task) {
        task.setId(++newId);
        tasks.put(newId, task);
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(++newId);
        epic.removeAllSubtasks();
        epics.put(newId, epic);
        return epic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        subtask.setId(++newId);
        subtasks.put(newId, subtask);
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtask(subtask.getId());
        calculateStatus(epic);
        return subtask;
    }

    @Override
    public void updateTask(Task task) {
        Task saved = tasks.get(task.getId());
        if (saved == null) {
            return;
        }
        saved.setName(task.getName());
        saved.setDescription(task.getDescription());
        saved.setStatus(task.getStatus());
    }

    @Override
    public void updateEpic(Epic epic) {
        Epic saved = epics.get(epic.getId());
        if (saved == null) {
            return;
        }
        saved.setName(epic.getName());
        saved.setDescription(epic.getDescription());
    }

    @Override
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

    @Override
    public void clearTasksList() {
        for (Integer id : tasks.keySet()) {
            historyManager.remove(id);
        }
        tasks.clear();
    }

    @Override
    public void clearEpicsList() {
        for (Integer id : epics.keySet()) {
            historyManager.remove(id);
        }
        for (Integer id : subtasks.keySet()) {
            historyManager.remove(id);
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void clearSubtasksList() {
        for (Integer id : subtasks.keySet()) {
            historyManager.remove(id);
        }
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.removeAllSubtasks();
            calculateStatus(epic);
        }
    }

    @Override
    public Task getTask(int id) {
        final Task task = tasks.get(id);
        historyManager.addTaskToHistory(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        final Epic epic = epics.get(id);
        historyManager.addTaskToHistory(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        final Subtask subtask = subtasks.get(id);
        historyManager.addTaskToHistory(subtask);
        return subtask;
    }

    @Override
    public List<Task> getTasksList() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpicsList() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getSubtasksList() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void removeTask(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void removeEpic(int id) {
        final Epic epic = epics.remove(id);
        for (Integer subtaskId : epic.getSubtasksIds()) {
            subtasks.remove(subtaskId);
            historyManager.remove(subtaskId);
        }
        historyManager.remove(id);
    }

    @Override
    public void removeSubtask(int id) {
        final Subtask subtask = subtasks.remove(id);
        historyManager.remove(id);
        Epic epic = epics.get(subtask.getEpicId());
        epic.removeSubtask(id);
        calculateStatus(epic);
    }

    @Override
    public List<Subtask> getEpicSubtasks(int id) {
        List<Subtask> epicSubtasks = new ArrayList<>();
        for (Integer subtaskId : epics.get(id).getSubtasksIds()) {
            if (subtasks.containsKey(subtaskId)) {
                epicSubtasks.add(subtasks.get(subtaskId));
            }
        }
        return epicSubtasks;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getTasks();
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
                epic.setStatus(Status.NEW);
                return;
            } else {
                status = Status.IN_PROGRESS;
            }
        }
        epic.setStatus(status);
    }
}