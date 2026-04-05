package com.finance.dashboard.service;

import com.finance.dashboard.dto.CreateUserRequest;
import com.finance.dashboard.dto.UpdateUserRequest;
import com.finance.dashboard.dto.UpdateProfileRequest;
import com.finance.dashboard.dto.UserResponse;
import com.finance.dashboard.exception.ApiException;
import com.finance.dashboard.model.Role;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.UserStatus;
import com.finance.dashboard.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse createUser(CreateUserRequest request) {
        return toResponse(createUserEntity(request.name(), request.email(), request.role(), request.status(), request.password()));
    }

    public User createUserEntity(String name, String email, Role role, UserStatus status, String rawPassword) {
        String normalizedEmail = email.trim().toLowerCase();
        userRepository.findByEmailIgnoreCase(normalizedEmail)
                .ifPresent(existing -> {
                    throw new ApiException(HttpStatus.CONFLICT, "Email already exists");
                });

        User user = new User();
        user.setName(name.trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setStatus(status);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        userRepository.findByEmailIgnoreCase(request.email())
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    throw new ApiException(HttpStatus.CONFLICT, "Email already exists");
                });

        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setRole(request.role());
        user.setStatus(request.status());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        user.setUpdatedAt(LocalDateTime.now());
        return toResponse(userRepository.save(user));
    }

    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        userRepository.findByEmailIgnoreCase(request.email().trim().toLowerCase())
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> {
                    throw new ApiException(HttpStatus.CONFLICT, "Email already exists");
                });

        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setUpdatedAt(LocalDateTime.now());
        return toResponse(userRepository.save(user));
    }

    public UserResponse getUser(Long userId) {
        return userRepository.findById(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public List<UserResponse> getActiveProfiles() {
        return userRepository.findAllByStatus(UserStatus.ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt()
        );
    }
}
