package au.com.galatexcollection.my_task_tracker.repository;

import au.com.galatexcollection.my_task_tracker.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
}
