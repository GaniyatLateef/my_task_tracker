package au.com.galatexcollection.my_task_tracker.security.service;


import au.com.galatexcollection.my_task_tracker.entity.User;
import au.com.galatexcollection.my_task_tracker.security.CustomUserDetails;
import au.com.galatexcollection.my_task_tracker.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public CustomUserDetailsService( final UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser = userService.getUserByIgnoreCaseUsername(username);

        return optionalUser.map(CustomUserDetails::createUserDetails).orElse(null);

    }

    public Optional<User> getUserById(String id) {
        return userService.getUserById(Integer.valueOf(id));
    }
}
