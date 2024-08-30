package model;

import java.util.ArrayList;

public class Epic extends Task {

    ArrayList<Integer> subtasksIds = new ArrayList<>();

    public Epic(String name, String description, Status status) {
        super(name, description, status);
    }

    public void addSubtask(int id) {
        subtasksIds.add(id);
    }

    public void removeSubtask(int id) {
        subtasksIds.remove(id);
    }

    public void removeAllSubtasks() {
        subtasksIds.clear();
    }

    public ArrayList<Integer> getSubtasksIds() {
        return subtasksIds;
    }
}
