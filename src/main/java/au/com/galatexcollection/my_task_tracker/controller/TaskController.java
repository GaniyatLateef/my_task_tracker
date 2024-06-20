package au.com.galatexcollection.my_task_tracker.controller;


import au.com.galatexcollection.my_task_tracker.entity.RoleName;
import au.com.galatexcollection.my_task_tracker.entity.Task;
import au.com.galatexcollection.my_task_tracker.model.TaskPayload;
import au.com.galatexcollection.my_task_tracker.security.CustomUserDetails;
import au.com.galatexcollection.my_task_tracker.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

import static java.lang.String.format;

@RestController
@RequestMapping("api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController( final TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    private ResponseEntity<List<TaskPayload>> getAllTasks(@AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean isAdmin = isAdminUser(userDetails);
        List<Task> dbTasks;

        if (isAdmin) {
            dbTasks = taskService.getAllTasks();
        } else {
            dbTasks = taskService.getAllUserTasks(userDetails.getId());
        }

        List<TaskPayload> taskPayloads = dbTasks
            .stream()
            .map(mapTaskToPayload() )
            .toList();

        return ResponseEntity.ok(taskPayloads);
    }

    private Function<Task, TaskPayload> mapTaskToPayload() {
        return task -> new TaskPayload(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getTaskDay(),
            task.getReminder(),
            task.getStatus(),
            task.getCompletedDate(),
            task.getCreatedDate(),
            task.getLastModifiedDate(),
            task.getUser().getName()
        );
    }

    @GetMapping("/{taskId}")
    private  ResponseEntity<TaskPayload> getTaskById(@AuthenticationPrincipal CustomUserDetails userDetails,@PathVariable Integer taskId) {
        boolean isAdmin = isAdminUser(userDetails);

        if (isAdmin){
            return taskService.getTaskById(taskId)
                .map(mapTaskToPayload())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
        }else {
            return taskService.getTaskByIdAndUserId(taskId, userDetails.getId())
                .map(mapTaskToPayload())
                .map(ResponseEntity::ok)
                .orElseGet(() ->ResponseEntity.notFound().build());
        }
    }

    @PostMapping
    private ResponseEntity<Void> addnewTask(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody Task task) throws URISyntaxException {
        //return new ResponseEntity<>(taskService.addTask(task), HttpStatus.CREATED);

         /*SecurityContext sc = SecurityContextHolder.getContext();
        var userDetails = (CustomUserDetails) sc.getAuthentication().getPrincipal();
        System.out.println(userDetails.getName());*/

        var taskCreated = taskService.addTask(task, userDetails.getUserFromUserDetails());

        return ResponseEntity
                .created(new URI(format("api/v1/tasks/%s", taskCreated.getId())))
                .build();
    }

    @PutMapping("/{taskId}")
    private ResponseEntity<TaskPayload> updateTask(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                   @RequestBody Task taskBody, @PathVariable Integer taskId) {

        return taskService.getTaskByIdAndUserId(taskId, userDetails.getId())
                .map(task -> {
                    task.setTitle(taskBody.getTitle());
                    task.setDescription(taskBody.getDescription());
                    task.setTaskDay(taskBody.getTaskDay());
                    task.setReminder(taskBody.getReminder());
                    return ResponseEntity.ok(mapTaskToPayload().apply(taskService.updateTask(task)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/status/{taskId}")
    private ResponseEntity<TaskPayload> updateTaskStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                  @PathVariable Integer taskId) {
        return taskService.getTaskByIdAndUserId(taskId, userDetails.getId())
                .map(task -> {
                    task.setStatus(!task.getStatus());
                    if (task.getStatus()) {
                        task.setCompletedDate(LocalDateTime.now());
                    }else {
                        task.setCompletedDate(null);
                    }

                    return ResponseEntity.ok(mapTaskToPayload().apply(taskService.updateTask(task)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/reminder/{taskId}")
    private ResponseEntity<TaskPayload> updateTaskReminder(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                           @PathVariable Integer taskId) {
        return taskService.getTaskByIdAndUserId(taskId, userDetails.getId())
                .map(task -> {
                    task.setReminder(!task.getReminder());
                    return ResponseEntity.ok(mapTaskToPayload().apply(taskService.updateTask(task)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @PathVariable Integer taskId) {
        if (isAdminUser(userDetails)) {
            taskService.deleteTask(taskId);
        }else {
             taskService.getTaskByIdAndUserId(taskId, userDetails.getId())
                 .ifPresent(task -> taskService.deleteTask(task.getId()
                 ));
        }
            return ResponseEntity.noContent().build();

    }

    private boolean isAdminUser(CustomUserDetails userDetails) {
        return userDetails.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals(RoleName.ROLE_ADMIN.name()));
    }
}
