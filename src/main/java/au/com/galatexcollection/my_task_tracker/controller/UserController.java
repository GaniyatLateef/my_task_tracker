package au.com.galatexcollection.my_task_tracker.controller;

import au.com.galatexcollection.my_task_tracker.entity.User;
import au.com.galatexcollection.my_task_tracker.exception.CustomException;
import au.com.galatexcollection.my_task_tracker.model.Login;
import au.com.galatexcollection.my_task_tracker.model.RegisterPayload;
import au.com.galatexcollection.my_task_tracker.security.service.TokenProviderService;
import au.com.galatexcollection.my_task_tracker.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.lang.String.format;


@RestController
@RequestMapping("api/v1/users")
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenProviderService tokenProviderService;

    public UserController(final UserService userService, final AuthenticationManager authenticationManager,
                          final TokenProviderService tokenProviderService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenProviderService = tokenProviderService;
    }

    @PostMapping("/auth/register")
    private ResponseEntity<?> registerUser(@RequestBody RegisterPayload payload) {
        if (payload.getPassword() != null && payload.getUsername() != null){
            if (userService.getUserByUsername(payload.getUsername()).isEmpty()) {
                return ResponseEntity.ok(userService.createUser(payload));
            }else {
                return new ResponseEntity<>(format("Username %s already exist", payload.getUsername()),
                    HttpStatus.BAD_REQUEST);

            }
        }else {
            return  new ResponseEntity<>("Username or password can not be empty",
                HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/auth/login")
    private ResponseEntity<?> loginUser(@RequestBody Login login) {

        try {
            var authRequest = new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword());
            Authentication authentication = authenticationManager.authenticate(authRequest);

            var tokenResponse = tokenProviderService.generateToken(authentication);

            if (tokenResponse != null) {
                return ResponseEntity.ok(tokenProviderService.generateToken(authentication));
            }
        }catch (Exception ex) {
            LOG.error(ex.getMessage());
        }

        var errorMessage = new CustomException(401, HttpStatus.UNAUTHORIZED.name(), "Invalid username or password");
        return new ResponseEntity<>( errorMessage, HttpStatus.UNAUTHORIZED);
    }
}
