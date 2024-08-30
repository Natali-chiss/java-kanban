package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.HashMap;

public class TaskManager {
    HashMap<Integer, Task> tasks;
    HashMap<Integer, Epic> epics;
    HashMap<Integer, Subtask> subtasks;

    public TaskManager() {
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
    }

    int newId = 0;

    public Task createTask(Task task) {
        new Task(task.getName(), task.getDescription(), task.getStatus());
        task.setId(++newId);
        tasks.put(newId, task);
        return task;
    }

    public Epic createEpic(Epic epic) {
        new Epic(epic.getName(), epic.getDescription(), epic.getStatus());
        epic.setId(++newId);
        epic.setStatus(Status.NEW);
        epic.removeAllSubtasks();
        epics.put(newId, epic);
        return epic;
    }

    public Subtask createSubtask(Subtask subtask) {
        new Subtask(subtask.getName(), subtask.getDescription(), subtask.getStatus(), subtask.getEpicId());
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
        calculateStatus(epic);
    }

    public void updateSubtask(Subtask subtask) {
        Subtask saved = subtasks.get(subtask.getId());
        if (saved == null) {
            return;
        }
        saved.setName(subtask.getName());
        saved.setDescription(subtask.getDescription());
        saved.setStatus(subtask.getStatus());
        saved.setEpicId(subtask.getEpicId());
        calculateStatus(getEpic(subtask.getEpicId()));
    }

    public void clearTasksList() {
        tasks.clear();
        epics.clear();
        subtasks.clear();
    }

    public Task get(int id) {
        return tasks.get(id);
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    public HashMap<Integer, Task> getTasksList() {
        return tasks;
    }

    public HashMap<Integer, Epic> getEpicsList() {
        return epics;
    }

    public HashMap<Integer, Subtask> getSubtasksList() {
        return subtasks;
    }

    public void removeTask(int id) {
        tasks.remove(id);
    }

    void removeEpic(int id) {
        epics.remove(id);
    }

    public void removeSubtask(int id) {
        Subtask subtask = getSubtask(id);
        Epic epic = getEpic(subtask.getEpicId());
        subtasks.remove(id);
        //epic.removeSubtask(id);
        calculateStatus(epic);
    }

    private void calculateStatus(Epic epic) {
        Status status = Status.NEW;
        int numberD = 0;
        int numberN = 0;
        for (Integer id : epic.getSubtasksIds()) {
            Subtask subtask = getSubtask(id);
            if(subtask != null) {
                if (subtask.getStatus() == Status.DONE) {
                    numberD++;
                } else if (subtask.getStatus() == Status.NEW) {
                    numberN++;
                }
                if (numberD == epic.getSubtasksIds().size()) {
                    status = Status.DONE;
                } else if (numberN == epic.getSubtasksIds().size()) {
                    status = Status.NEW;
                } else {
                    status = Status.IN_PROGRESS;
                }
            }
        }
            epic.setStatus(status);
    }
}
