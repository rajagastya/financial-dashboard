package com.finance.dashboard.dto;

import com.finance.dashboard.model.Role;
import com.finance.dashboard.model.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateUserRequest(
        @NotBlank(message = "Name is required")
        String name,
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,
        @NotNull(message = "Role is required")
        Role role,
        @NotNull(message = "Status is required")
        UserStatus status,
        @Pattern(
                regexp = "^$|(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "Password must be blank or at least 8 characters with uppercase, lowercase, and a number"
        )
        String password
) {
}
