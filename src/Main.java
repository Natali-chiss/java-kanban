import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import service.TaskManager;

public class Main {

    public static void main(String[] args) {
        TaskManager tm = new TaskManager();

        Task task = tm.createTask(new Task("Задача", "1", Status.NEW));
        System.out.println("Create " + task);
        Task task2 = tm.createTask(new Task("Задача", "2", Status.NEW));
        System.out.println("Create " + task2);
        Task taskFromTm = tm.get(task.getId());
        System.out.println("Get " + taskFromTm);
        taskFromTm.setName("Новое имя задачи");
        tm.updateTask(taskFromTm);
        System.out.println("Update " + taskFromTm);
        tm.removeTask(taskFromTm.getId());
        System.out.println(tm.getTasksList());


        Epic epic1 = tm.createEpic(new Epic("epic", "1", Status.NEW));
        Subtask subtask1 = tm.createSubtask(new Subtask("subtask", "1", Status.NEW, epic1.getId()));
        Subtask subtask2 = tm.createSubtask(new Subtask("subtask", "2", Status.NEW, epic1.getId()));

        Epic epic2 = tm.createEpic(new Epic("epic", "2", Status.NEW));
        Subtask subtask3 = tm.createSubtask(new Subtask("subtask", "3", Status.NEW, epic2.getId()));
        subtask3.setStatus(Status.DONE);
        tm.updateSubtask(subtask3);
        System.out.println(tm.getEpicsList());
        System.out.println(tm.getSubtasksList());
        tm.removeSubtask(subtask3.getId());
        System.out.println(tm.getEpicsList());
        System.out.println(tm.getSubtasksList());
    }
}
