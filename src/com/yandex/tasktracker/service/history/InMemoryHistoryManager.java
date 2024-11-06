package com.yandex.tasktracker.service.history;

import com.yandex.tasktracker.model.Task;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node> historyMap = new HashMap<>();
    private Node first;
    private Node last;

    private static class Node {
        Node prev;
        Task task;
        Node next;

        Node(Node prev, Task task, Node next) {
            this.prev = prev;
            this.task = task;
            this.next = next;
        }
    }

    @Override
    public void addTaskToHistory(Task task) {
        removeNode(historyMap.get(task.getId()));
        Task savedTask = new Task(task.getName(), task.getDescription(), task.getStatus(), task.getDuration(), task.getStartTime());
        savedTask.setId(task.getId());
        linkLast(savedTask);
        historyMap.put(savedTask.getId(), last);
    }

    public void remove(int id) {
        removeNode(historyMap.get(id));
        historyMap.remove(id);
    }

    public List<Task> getTasks() {
        final List<Task> historyList = new LinkedList<>();
        Node current = first;
        while (current != null) {
            historyList.addLast(current.task);
            current = current.next;
        }
        return historyList;
    }

    private void linkLast(Task task) {
        final Node l = last;
        final Node newNode = new Node(l, task, null);
        last = newNode;
        if (l == null)
            first = newNode;
        else
            l.next = newNode;
        historyMap.put(task.getId(), last);
    }

    private void removeNode(Node node) {
        if (node == null) {
            return;
        }

        final Node prev = node.prev;
        final Node next = node.next;

        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            node.prev = null;
        }

        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            node.next = null;
        }

        node.task = null;
    }
}
