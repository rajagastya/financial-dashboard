package com.finance.dashboard.service;

import com.finance.dashboard.dto.AuthResponse;
import com.finance.dashboard.dto.ChangePasswordRequest;
import com.finance.dashboard.dto.ForgotPasswordRequest;
import com.finance.dashboard.dto.LoginRequest;
import com.finance.dashboard.dto.LogoutRequest;
import com.finance.dashboard.dto.MessageResponse;
import com.finance.dashboard.dto.RefreshTokenRequest;
import com.finance.dashboard.dto.ResetPasswordRequest;
import com.finance.dashboard.dto.SignupRequest;
import com.finance.dashboard.dto.UpdateProfileRequest;
import com.finance.dashboard.dto.UserResponse;
import com.finance.dashboard.exception.ApiException;
import com.finance.dashboard.model.PasswordResetToken;
import com.finance.dashboard.model.RefreshToken;
import com.finance.dashboard.model.Role;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.UserStatus;
import com.finance.dashboard.repository.PasswordResetTokenRepository;
import com.finance.dashboard.repository.RefreshTokenRepository;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.security.AccessControlService;
import com.finance.dashboard.security.JwtProperties;
import com.finance.dashboard.security.JwtService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final AccessControlService accessControlService;
    private final UserService userService;
    private final long passwordResetExpirationMs;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       JwtProperties jwtProperties,
                       AccessControlService accessControlService,
                       UserService userService,
                       @Value("${app.auth.password-reset-expiration-ms}") long passwordResetExpirationMs) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.accessControlService = accessControlService;
        this.userService = userService;
        this.passwordResetExpirationMs = passwordResetExpirationMs;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        validatePasswordConfirmation(request.password(), request.confirmPassword());
        User user = userService.createUserEntity(
                request.name(),
                request.email(),
                Role.VIEWER,
                UserStatus.ACTIVE,
                request.password()
        );
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Inactive users cannot sign in");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        revokeActiveRefreshTokens(user);
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token has expired");
        }

        User user = refreshToken.getUser();
        if (user.getStatus() != UserStatus.ACTIVE || !jwtService.isTokenValid(refreshToken.getToken(), user, "refresh")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        revokeRefreshToken(refreshToken);
        return issueTokens(user);
    }

    @Transactional
    public MessageResponse logout(LogoutRequest request) {
        refreshTokenRepository.findByToken(request.refreshToken()).ifPresent(this::revokeRefreshToken);
        return new MessageResponse("Logged out successfully", null);
    }

    public UserResponse getCurrentUser() {
        return userService.toResponse(accessControlService.getCurrentUser());
    }

    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User currentUser = accessControlService.getCurrentUser();
        return userService.updateProfile(currentUser.getId(), request);
    }

    @Transactional
    public MessageResponse changePassword(ChangePasswordRequest request) {
        validatePasswordConfirmation(request.newPassword(), request.confirmPassword());
        User currentUser = accessControlService.getCurrentUser();

        if (!passwordEncoder.matches(request.currentPassword(), currentUser.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        if (passwordEncoder.matches(request.newPassword(), currentUser.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "New password must be different from the current password");
        }

        currentUser.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        currentUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(currentUser);
        revokeActiveRefreshTokens(currentUser);
        return new MessageResponse("Password updated successfully", null);
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        return userRepository.findByEmailIgnoreCase(request.email().trim().toLowerCase())
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .map(this::createPasswordResetToken)
                .orElse(new MessageResponse(
                        "If the account exists, a reset link has been generated for development use.",
                        null
                ));
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        validatePasswordConfirmation(request.newPassword(), request.confirmPassword());

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid reset token"));

        if (resetToken.getUsedAt() != null || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);
        revokeActiveRefreshTokens(user);

        return new MessageResponse("Password reset successfully", null);
    }

    private MessageResponse createPasswordResetToken(User user) {
        List<PasswordResetToken> existing = passwordResetTokenRepository.findAllByUserAndUsedAtIsNull(user);
        for (PasswordResetToken token : existing) {
            token.setUsedAt(LocalDateTime.now());
        }
        passwordResetTokenRepository.saveAll(existing);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setUser(user);
        resetToken.setCreatedAt(LocalDateTime.now());
        resetToken.setExpiresAt(LocalDateTime.now().plusNanos(passwordResetExpirationMs * 1_000_000));
        passwordResetTokenRepository.save(resetToken);

        return new MessageResponse(
                "If the account exists, a reset link has been generated for development use.",
                resetToken.getToken()
        );
    }

    private void validatePasswordConfirmation(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password and confirm password must match");
        }
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken storedRefreshToken = new RefreshToken();
        storedRefreshToken.setToken(refreshToken);
        storedRefreshToken.setUser(user);
        storedRefreshToken.setCreatedAt(LocalDateTime.now());
        storedRefreshToken.setExpiresAt(LocalDateTime.now().plusNanos(jwtProperties.refreshTokenExpirationMs() * 1_000_000));
        storedRefreshToken.setRevoked(false);
        refreshTokenRepository.save(storedRefreshToken);

        return new AuthResponse(
                accessToken,
                refreshToken,
                jwtProperties.accessTokenExpirationMs() / 1000,
                userService.toResponse(user)
        );
    }

    private void revokeActiveRefreshTokens(User user) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findAllByUserAndRevokedFalse(user);
        for (RefreshToken token : activeTokens) {
            revokeRefreshToken(token);
        }
    }

    private void revokeRefreshToken(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);
    }
}
