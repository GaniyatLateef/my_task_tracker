package au.com.galatexcollection.my_task_tracker.service;


import au.com.galatexcollection.my_task_tracker.entity.Task;
import au.com.galatexcollection.my_task_tracker.entity.User;
import au.com.galatexcollection.my_task_tracker.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService( final TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Optional<Task> getTaskById(Integer taskId) {
        return taskRepository.findById(taskId);
    }

    public Task addTask(Task task, User user) {
        task.setStatus(false);
        task.setUser(user);

        return taskRepository.save(task);
    }

    public List<Task> getAllUserTasks(Integer userId) {
        return taskRepository.findByUserId(userId);
    }

    public Optional<Task> getTaskByIdAndUserId(Integer taskId, Integer userId) {
       return taskRepository.findByIdAndUserId(taskId, userId);
    }

    public Task updateTask(Task task) {
        return taskRepository.save(task);
    }

    public void deleteTask(Integer taskId) {
        taskRepository.deleteById(taskId);
    }
}
