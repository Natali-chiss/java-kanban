package com.yandex.tasktracker.service;

import com.yandex.tasktracker.model.Epic;
import com.yandex.tasktracker.model.Status;
import com.yandex.tasktracker.model.Subtask;
import com.yandex.tasktracker.model.Task;
import com.yandex.tasktracker.service.exceptions.NotFoundException;
import com.yandex.tasktracker.service.exceptions.ValidationException;
import com.yandex.tasktracker.service.history.HistoryManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected final Map<Integer, Task> tasks;
    protected final Map<Integer, Epic> epics;
    protected final Map<Integer, Subtask> subtasks;
    private final HistoryManager historyManager;
    protected final Set<Task> prioritizedTasks;

    protected int newId = 0;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.prioritizedTasks = new TreeSet<>(Task::compareByStartTime);
    }

    @Override
    public Task createTask(Task task) {
        task.setId(++newId);
        tasks.put(newId, task);
        if (task.getStartTime() != null) {
            checkNoTimeConflict(task);
            prioritizedTasks.add(task);
        }
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
        if (subtask.getStartTime() != null) {
            checkNoTimeConflict(subtask);
            prioritizedTasks.add(subtask);
        }
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtask(subtask.getId());
        calculateEpic(epic);
        return subtask;
    }

    @Override
    public void updateTask(Task task) {
        Task saved = tasks.get(task.getId());
        if (saved == null) {
            throw new NotFoundException("Task id=" + task.getId());
        }
        if (task.getStartTime() != null) {
            checkNoTimeConflict(task);
            prioritizedTasks.remove(saved);
            prioritizedTasks.add(task);
        }
        tasks.put(task.getId(), task);
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
            throw new NotFoundException("Subtask id=" + subtask.getId());
        }
        if (subtask.getStartTime() != null) {
            checkNoTimeConflict(subtask);
            prioritizedTasks.remove(saved);
            prioritizedTasks.add(subtask);
        }
        subtasks.put(subtask.getId(), subtask);
        calculateEpic(epics.get(subtask.getEpicId()));
    }

    @Override
    public void clearTasksList() {
        for (Task task : tasks.values()) {
            historyManager.remove(task.getId());
            prioritizedTasks.remove(task);
        }
        tasks.clear();
    }

    @Override
    public void clearEpicsList() {
        for (Integer id : epics.keySet()) {
            historyManager.remove(id);
        }
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
            prioritizedTasks.remove(subtask);
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void clearSubtasksList() {
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
            prioritizedTasks.remove(subtask);
        }
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.removeAllSubtasks();
            calculateEpic(epic);
        }
    }

    @Override
    public Task getTask(int id) {
        final Task task = tasks.get(id);
        if (task == null) {
            throw new NotFoundException("Task id=" + id + " не найдена");
        }
        historyManager.addTaskToHistory(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        final Epic epic = epics.get(id);
        if (epic == null) {
            throw new NotFoundException("Epic id=" + id + " не найден");
        }
        historyManager.addTaskToHistory(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        final Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new NotFoundException("Subtask id=" + id + " не найдена");
        }
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

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public void removeTask(int id) {
        final Task task = tasks.remove(id);
        if (task.getStartTime() != null) {
            prioritizedTasks.remove(task);
        }
        historyManager.remove(id);
    }

    @Override
    public void removeEpic(int id) {
        final Epic epic = epics.remove(id);
        for (Integer subtaskId : epic.getSubtasksIds()) {
            Subtask subtask = subtasks.remove(subtaskId);
            if (subtask.getStartTime() != null) {
                prioritizedTasks.remove(subtask);
            }
            historyManager.remove(subtaskId);
        }
        historyManager.remove(id);
    }

    @Override
    public void removeSubtask(int id) {
        final Subtask subtask = subtasks.remove(id);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.remove(subtask);
        }
        historyManager.remove(id);
        Epic epic = epics.get(subtask.getEpicId());
        epic.removeSubtask(id);
        calculateEpic(epic);
    }

    @Override
    public List<Subtask> getEpicSubtasks(int id) {
        return epics.get(id).getSubtasksIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getTasks();
    }

    protected void calculateEpic(Epic epic) {
        calculateStatus(epic);
        calculateEpicTime(epic);
    }

    private void calculateEpicTime(Epic epic) {
        LocalDateTime earliest = null;
        LocalDateTime currentStartTime;
        LocalDateTime latest = null;
        LocalDateTime currentEndTime;
        Duration duration = null;

        if (!epic.getSubtasksIds().isEmpty()) {
            for (Integer subtaskId : epic.getSubtasksIds()) {
                Subtask subtask = subtasks.get(subtaskId);

                if (subtask.getDuration() != null) {
                    if (duration == null) {
                        duration = subtask.getDuration();
                    } else {
                        duration = duration.plus(subtask.getDuration());
                    }
                }

                currentStartTime = subtask.getStartTime();
                if (currentStartTime != null) {
                    if (earliest == null || currentStartTime.isBefore(earliest)) {
                        earliest = currentStartTime;
                    }
                }

                currentEndTime = subtask.getEndTime();
                if (currentEndTime != null) {
                    if (latest == null || currentEndTime.isAfter(latest)) {
                        latest = currentEndTime;
                    }
                }
            }
        }
        epic.setStartTime(earliest);
        epic.setEndTime(latest);
        epic.setDuration(duration);
    }

    private void calculateStatus(Epic epic) {
        List<Subtask> subtasksList = epic.getSubtasksIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .toList();

        long numberD = subtasksList.stream()
                .filter(subtask -> subtask.getStatus() == Status.DONE)
                .count();

        long numberN = subtasksList.stream()
                .filter(subtask -> subtask.getStatus() == Status.NEW)
                .count();

        if (subtasksList.isEmpty()) {
            epic.setStatus(Status.NEW);
        } else if (numberD == subtasksList.size()) {
            epic.setStatus(Status.DONE);
        } else if (numberN == subtasksList.size()) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    private void checkNoTimeConflict(Task task) {
        for (Task t : getPrioritizedTasks()) {
            if (t.getId() == task.getId()) {
                continue;
            }
            if (!t.getEndTime().isAfter(task.getStartTime()) || !task.getEndTime().isAfter(t.getStartTime())) {
                continue;
            }
            throw new ValidationException("Пересечение задач с id=" + task.getId() + " и id=" + t.getId());
        }
    }
}