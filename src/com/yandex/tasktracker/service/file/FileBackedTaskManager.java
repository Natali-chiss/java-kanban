package com.yandex.tasktracker.service.file;

import com.yandex.tasktracker.model.*;
import com.yandex.tasktracker.service.InMemoryTaskManager;
import com.yandex.tasktracker.service.Managers;
import com.yandex.tasktracker.service.TaskManager;
import com.yandex.tasktracker.service.history.HistoryManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Files;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {

    private final Path path;

    public FileBackedTaskManager(Path path) {
        super(Managers.getDefaultHistory());
        this.path = path;
    }

    public FileBackedTaskManager() {
        super(Managers.getDefaultHistory());
        try {
            path = Files.createFile(Path.of("src/com/yandex/tasktracker/service/file/newFile"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public FileBackedTaskManager(Path path, HistoryManager historyManager) {
        super(historyManager);
        this.path = path;
    }

    private void save() {
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile(),
                StandardCharsets.UTF_8))) {
            writer.write("id,type,name,status,description,epic\n");
            for (Task task : tasks.values()) {
                writer.write(toString(task) + "\n");
            }
            for (Epic epic : epics.values()) {
                writer.write(toString(epic) + "\n");
            }
            for (Subtask subtask : subtasks.values()) {
                writer.write(toString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка в файле: " + path, e);
        }
    }

    public void init() {
        loadFromFile(path);
    }

    private FileBackedTaskManager loadFromFile(Path file) {
        final FileBackedTaskManager fileManager = new FileBackedTaskManager(file);
        try (BufferedReader reader = new BufferedReader(new FileReader(file.toFile(), StandardCharsets.UTF_8))) {
            while (reader.ready()) {
                String line = reader.readLine();
                if (!line.equals("id,type,name,status,description,epic")) {
                    Task task = fromString(line);
                    if (task != null) {
                        switch (task.getType()) {
                            case TASK -> tasks.put(task.getId(), task);
                            case EPIC -> epics.put(task.getId(), (Epic) task);
                            case SUBTASK -> subtasks.put(task.getId(), (Subtask) task);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка в файле " + file, e);
        }
        return fileManager;
    }

    private Task fromString(String value) {
        if (value.isBlank()) {
            return null;
        }
        final String[] traits = value.split(",");

        Task task;
        Type type = Type.valueOf(traits[1]);
        String name = traits[2];
        String description = traits[4];
        Status status = Status.valueOf(traits[3]);

        switch (type) {
            case TASK:
                task = new Task(name, description, status);
                break;

            case EPIC:
                task = new Epic(name, description);
                task.setStatus(status);
                break;

            case SUBTASK:
                task = new Subtask(name, description, status, Integer.parseInt(traits[5]));
                break;

            default:
                task = null;
        }

        if (task != null) {
            task.setId(Integer.parseInt(traits[0]));
        }
        return task;
    }

    @Override
    public Task createTask(Task task) {
        super.createTask(task);
        save();
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        super.createEpic(epic);
        save();
        return epic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
        return subtask;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void clearTasksList() {
        super.clearTasksList();
        save();
    }

    @Override
    public void clearEpicsList() {
        super.clearEpicsList();
        save();
    }

    @Override
    public void clearSubtasksList() {
        super.clearSubtasksList();
        save();
    }

    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void removeEpic(int id) {
        super.removeEpic(id);
        save();
    }

    @Override
    public void removeSubtask(int id) {
        super.removeSubtask(id);
        save();
    }

    private String toString(Task task) {
        return task.getId() + "," + task.getType() + "," + task.getName() + "," + task.getStatus() + ","
                + task.getDescription() + "," + task.getEpicId();
    }
}
