package au.com.galatexcollection.my_task_tracker.controller;


import au.com.galatexcollection.my_task_tracker.entity.Task;
import au.com.galatexcollection.my_task_tracker.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;

import static java.lang.String.format;

@RestController
@RequestMapping("api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController( final TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    private ResponseEntity<List<Task>> getAllTasks() {

        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{taskId}")
    private  ResponseEntity<Task> getTaskById(@PathVariable Integer taskId) {
        return taskService.getTaskById(taskId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    private ResponseEntity<Void> addnewTask(@RequestBody Task task) throws URISyntaxException {
        //return new ResponseEntity<>(taskService.addTask(task), HttpStatus.CREATED);
        var taskCreated = taskService.addTask(task);
        return ResponseEntity
                .created(new URI(format("api/v1/tasks/%s", taskCreated.getId())))
                .build();
    }

    @PutMapping("/{taskId}")
    private ResponseEntity<Task> updateTask(@RequestBody Task taskBody, @PathVariable Integer taskId) {
        return taskService.getTaskById(taskId)
                .map(task -> {
                    task.setTitle(taskBody.getTitle());
                    task.setDescription(taskBody.getDescription());
                    task.setTaskDay(taskBody.getTaskDay());
                    task.setReminder(taskBody.getReminder());
                    return ResponseEntity.ok(taskService.updateTask(task));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/status/{taskId}")
    private ResponseEntity<Task> updateTaskStatus(@PathVariable Integer taskId) {
        return taskService.getTaskById(taskId)
                .map(task -> {
                    task.setStatus(!task.getStatus());
                    if (task.getStatus()) {
                        task.setCompletedDate(LocalDateTime.now());
                    }else {
                        task.setCompletedDate(null);
                    }

                    return ResponseEntity.ok(taskService.updateTask(task));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/reminder/{taskId}")
    private ResponseEntity<Task> updateTaskReminder(@PathVariable Integer taskId) {
        return taskService.getTaskById(taskId)
                .map(task -> {
                    task.setReminder(!task.getReminder());
                    return ResponseEntity.ok(taskService.updateTask(task));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Integer taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }




}
