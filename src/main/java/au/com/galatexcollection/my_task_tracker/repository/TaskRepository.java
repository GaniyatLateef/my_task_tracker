package au.com.galatexcollection.my_task_tracker.repository;

import au.com.galatexcollection.my_task_tracker.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByUserId(Integer userId);
    Optional<Task> findByIdAndUserId(Integer taskId, Integer userId);
}
