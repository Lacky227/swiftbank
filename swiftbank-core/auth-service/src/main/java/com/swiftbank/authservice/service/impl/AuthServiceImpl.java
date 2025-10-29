package com.swiftbank.authservice.service.impl;

import com.swiftbank.authservice.dto.*;
import com.swiftbank.authservice.models.DeviceInfo;
import com.swiftbank.authservice.models.Token;
import com.swiftbank.authservice.models.User;
import com.swiftbank.authservice.models.enumModel.UserRole;
import com.swiftbank.authservice.repository.AuthRepository;
import com.swiftbank.authservice.repository.TokenRepository;
import com.swiftbank.authservice.service.AuthService;
import com.swiftbank.authservice.service.JwtService;
import com.swiftbank.authservice.service.RabbitMQService;
import com.swiftbank.authservice.service.RedisService;
import com.swiftbank.authservice.utils.HashUtils;
import com.swiftbank.authservice.utils.ValidationUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthRepository authRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RabbitMQService rabbitMQService;
    private final RedisService  redisService;

    @Override
    public ResponseEntity<?> getUser(Long userId) {
        Optional<User> optionalUser = authRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = optionalUser.get();
        UserResponse userResponse = UserResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .locale(user.getLocale())
                .build();
        return ResponseEntity.ok(userResponse);
    }

    @Override
    public ResponseEntity<?> register(RegisterRequest request) {
        if (ValidationUtils.firstNameInvalid(request.getFirstName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("First name is required");
        }
        if (ValidationUtils.lastNameInvalid(request.getLastName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Last name is invalid");
        }
        if (ValidationUtils.emailInvalid(request.getEmail()) || authRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is invalid");
        }
        if (ValidationUtils.passwordInvalid(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password is invalid");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .locale(request.getLocale())
                .timezone(request.getTimezone())
                .country(request.getCountry())
                .acceptTerms(request.isAcceptTerms())
                .marketingConsent(request.isMarketingConsent())
                .registrationSource(request.getRegistrationSource())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .role(UserRole.USER)
                .active(true)
                .build();
        DeviceInfo devices =  DeviceInfo.builder()
                .deviceId(request.getDeviceInfo().getDeviceId())
                .deviceType(request.getDeviceInfo().getDeviceType())
                .os(request.getDeviceInfo().getOs())
                .osVersion(request.getDeviceInfo().getOsVersion())
                .appVersion(request.getDeviceInfo().getAppVersion())
                .userAgent(request.getDeviceInfo().getUserAgent())
                .ipAddress(request.getDeviceInfo().getIpAddress())
                .registeredAt(LocalDateTime.now())
                .lastUsedAt(LocalDateTime.now())
                .trusted(true)
                .user(user)
                .build();
        user.setDevices(List.of(devices));

        Token token = Token.builder()
                .refreshToken(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusDays(60))
                .user(user)
                .build();
        user.setToken(token);

        authRepository.save(user);

        rabbitMQService.sendCreateAccount(
                UserRegisteredPayload.builder()
                        .userId(user.getId())
                        .createdAt(user.getCreatedAt())
                        .build()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(
                jwtService.generateToken(user.getId(), user.getRole().toString()),
                token.getRefreshToken(),
                user.getRole().toString()
        ));
    }

    @Override
    public ResponseEntity<?> login(LoginRequest request) {
        if (ValidationUtils.emailInvalid(request.getEmail()) || ValidationUtils.passwordInvalid(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email or password");
        }
        Optional<User> userOpt = authRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid email or password");
        }

        User user = userOpt.get();
        Token token = Token.builder()
                .refreshToken(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusDays(60))
                .user(user)
                .build();
        user.setToken(token);

        authRepository.save(user);
        return ResponseEntity.status(HttpStatus.OK).body(new AuthResponse(
                jwtService.generateToken(user.getId(), user.getRole().toString()),
                token.getRefreshToken(),
                user.getRole().toString()
        ));
    }

    @Override
    public ResponseEntity<?> refresh(RefreshTokenRequest request) {
        Optional<Token> foundToken = tokenRepository.findByRefreshToken(request.getRefreshToken());
        if (foundToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (foundToken.get().getExpiresAt().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(foundToken.get());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User user = foundToken.get().getUser();

        foundToken.get().setRefreshToken(UUID.randomUUID().toString());
        tokenRepository.save(foundToken.get());

        return ResponseEntity.status(HttpStatus.OK).body(new AuthResponse(
                jwtService.generateToken(user.getId(), user.getRole().toString()),
                foundToken.get().getRefreshToken(),
                user.getRole().toString()
        ));
    }

    @Override
    public ResponseEntity<?> forgotPassword(ForgotRequest request) {
        Optional<User> user = authRepository.findByEmail(request.getEmail());
        if (user.isEmpty()) {
            return ResponseEntity.ok("If an account with this email exists, a password reset link has been sent.");
        }
        DeviceInfo device = user.get().getDevices().stream()
                .filter(d -> Objects.equals(d.getIpAddress(), request.getIpAddress()) && Objects.equals(d.getUserAgent(), request.getUserAgent())
                ).findFirst().orElse(null);
        if (device == null) {
            return ResponseEntity.ok("If an account with this email exists, a password reset link has been sent.");
        }
        ResetPayload resetPayload = ResetPayload.builder()
                .email(request.getEmail())
                .resetToken(UUID.randomUUID().toString())
                .locale(request.getLocale())
                .build();
        rabbitMQService.sendResetPassword(resetPayload);
        return ResponseEntity.ok("If an account with this email exists, a password reset link has been sent.");
    }

    @Override
    public ResponseEntity<?> resetPassword(ResetRequest request) {
        if (request.getToken() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token");
        }
        String hashToken = HashUtils.sha256(request.getToken());
        String email = redisService.getValue(hashToken);
        Optional<User> userOpt = (email != null) ? authRepository.findByEmail(email) : Optional.empty();
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token");
        }
        if (ValidationUtils.passwordInvalid(request.getNewPassword())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("New password is invalid");
        }
        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        String refreshToken = UUID.randomUUID().toString();
        user.getToken().setRefreshToken(refreshToken);
        authRepository.save(user);
        redisService.deleteValue(hashToken);
        return ResponseEntity.status(HttpStatus.OK).body(new AuthResponse(
                jwtService.generateToken(user.getId(), user.getRole().toString()),
                refreshToken,
                user.getRole().toString()
        ));
    }
}
