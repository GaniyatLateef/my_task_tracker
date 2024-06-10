package au.com.galatexcollection.my_task_tracker.repository;

import au.com.galatexcollection.my_task_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByIgnoreCaseUsername (String username);
}
