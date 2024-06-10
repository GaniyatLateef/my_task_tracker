package au.com.galatexcollection.my_task_tracker.service;

import au.com.galatexcollection.my_task_tracker.entity.RoleName;
import au.com.galatexcollection.my_task_tracker.entity.User;
import au.com.galatexcollection.my_task_tracker.model.RegisterPayload;
import au.com.galatexcollection.my_task_tracker.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
     private final UserRepository userRepository;
     private final PasswordEncoder passwordEncoder;

    public UserService(final UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> getUserById(Integer userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByUsername(String username) {

        return userRepository.findByUsername(username);
    }
    public  Optional<User> getUserByIgnoreCaseUsername(String username) {
        return userRepository.findByIgnoreCaseUsername(username);
    }

    public User createUser(RegisterPayload payload) {
        var user = new User();
        user.setUsername(payload.getUsername());
        user.setName(payload.getName());
        user.setPassword(passwordEncoder.encode(payload.getPassword()));

        Set<RoleName> roles = new HashSet<>();
        roles.add(RoleName.ROLE_USER);

        user.setRoles(roles);
        return userRepository.save(user);
    }
}
