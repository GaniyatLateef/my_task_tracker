package au.com.galatexcollection.my_task_tracker.model;

import java.time.LocalDateTime;

public record TaskPayload (
    Integer id,
    String title,
    String description,
    String taskDay,
    Boolean reminder,
    Boolean status,
    LocalDateTime completedDate,
    LocalDateTime createdDate,
    LocalDateTime lastModifiedDate,
    String user

){
}
