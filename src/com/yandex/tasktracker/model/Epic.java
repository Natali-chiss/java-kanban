package com.yandex.tasktracker.model;

import java.util.ArrayList;

public class Epic extends Task {

    private final ArrayList<Integer> subtasksIds = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
    }

    public void addSubtask(int id) {
        subtasksIds.add(id);
    }

    public void removeSubtask(Integer id) {
        subtasksIds.remove(id);
    }

    public void removeAllSubtasks() {
        subtasksIds.clear();
    }

    public ArrayList<Integer> getSubtasksIds() {
        return subtasksIds;
    }
}