package com.finance.dashboard.security;

import com.finance.dashboard.exception.ApiException;
import com.finance.dashboard.model.Role;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.UserStatus;
import com.finance.dashboard.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AccessControlService {

    private final UserRepository userRepository;

    public AccessControlService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Inactive users cannot access the system");
        }

        return user;
    }

    public User requireAnyRole(Role... roles) {
        User user = getCurrentUser();
        for (Role role : roles) {
            if (user.getRole() == role) {
                return user;
            }
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission for this action");
    }

    public User requireAdmin() {
        return requireAnyRole(Role.ADMIN);
    }
}
