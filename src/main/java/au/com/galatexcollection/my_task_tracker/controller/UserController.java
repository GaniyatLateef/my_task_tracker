package au.com.galatexcollection.my_task_tracker.controller;

import au.com.galatexcollection.my_task_tracker.entity.RoleName;
import au.com.galatexcollection.my_task_tracker.entity.User;
import au.com.galatexcollection.my_task_tracker.exception.CustomException;
import au.com.galatexcollection.my_task_tracker.model.Login;
import au.com.galatexcollection.my_task_tracker.model.RegisterPayload;
import au.com.galatexcollection.my_task_tracker.security.CustomUserDetails;
import au.com.galatexcollection.my_task_tracker.security.service.TokenProviderService;
import au.com.galatexcollection.my_task_tracker.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PatchMapping("/toggle/access/{userId}")
    private ResponseEntity<?> toggleUserAccess(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Integer userId) {
        if (!userId.equals(userDetails.getId())) {

            var optUser = userService.getUserById(userId);

            if (optUser.isPresent()) {
                var user = optUser.get();

                if (user.getRoles().contains(RoleName.ROLE_ACCESS)) {
                    user.getRoles().remove(RoleName.ROLE_ACCESS);
                } else {
                    user.getRoles().add(RoleName.ROLE_ACCESS);
                }

                return ResponseEntity.ok(userService.updateUser(user));
            } else {
                var errResponse = new CustomException(404, HttpStatus.NOT_FOUND.name(), "User with id " + userId + " not found.");
                return new ResponseEntity<>(errResponse, HttpStatus.NOT_FOUND);
            }
        } else {
            var errResponse = new CustomException(403, HttpStatus.FORBIDDEN.name(), "Cannot update the current user roles");
            return new ResponseEntity<>(errResponse, HttpStatus.FORBIDDEN);
        }
    }

    @PatchMapping("/toggle/admin/{userId}")
    private ResponseEntity<?> toggleUserAdmin(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Integer userId) {
        if (!userId.equals(userDetails.getId())) {

            var optUser = userService.getUserById(userId);

            if (optUser.isPresent()) {
                var user = optUser.get();

                if (user.getRoles().contains(RoleName.ROLE_ADMIN)) {
                    user.getRoles().remove(RoleName.ROLE_ADMIN);
                } else {
                    user.getRoles().add(RoleName.ROLE_ADMIN);
                }

                return ResponseEntity.ok(userService.updateUser(user));
            } else {
                var errResponse = new CustomException(404, HttpStatus.NOT_FOUND.name(), "User with id " + userId + " not found.");
                return new ResponseEntity<>(errResponse, HttpStatus.NOT_FOUND);
            }
        } else {
            var errResponse = new CustomException(403, HttpStatus.FORBIDDEN.name(), "Cannot update the current user roles");
            return new ResponseEntity<>(errResponse, HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/profile")
    private ResponseEntity<User> getUserProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userService.getUserById(userDetails.getId())
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    private ResponseEntity<List<User>> getAllUserProfiles(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userService.getAllUsersExceptAdmin(userDetails.getId()));
    }
}
