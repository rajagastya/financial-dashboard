package com.finance.dashboard.controller;

import com.finance.dashboard.dto.CreateUserRequest;
import com.finance.dashboard.dto.UpdateUserRequest;
import com.finance.dashboard.dto.UserResponse;
import com.finance.dashboard.security.AccessControlService;
import com.finance.dashboard.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AccessControlService accessControlService;

    public UserController(UserService userService, AccessControlService accessControlService) {
        this.userService = userService;
        this.accessControlService = accessControlService;
    }

    @GetMapping
    public List<UserResponse> getUsers() {
        accessControlService.requireAdmin();
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        accessControlService.requireAdmin();
        return userService.getUser(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        accessControlService.requireAdmin();
        return userService.createUser(createUserRequest);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id,
                                   @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        accessControlService.requireAdmin();
        return userService.updateUser(id, updateUserRequest);
    }
}
