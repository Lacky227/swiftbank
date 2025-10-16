package com.swiftbank.authservice.service.impl;

import com.swiftbank.JwtService;
import com.swiftbank.authservice.dto.*;
import com.swiftbank.authservice.models.DeviceInfo;
import com.swiftbank.authservice.models.Token;
import com.swiftbank.authservice.models.User;
import com.swiftbank.authservice.models.enumModel.UserRole;
import com.swiftbank.authservice.repository.AuthRepository;
import com.swiftbank.authservice.service.AuthService;
import com.swiftbank.authservice.utils.ValidationUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public ResponseEntity<?> register(RegisterRequest request) {
        if (ValidationUtils.firstNameInvalid(request.getFirstName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("First name is required");
        }
        if (ValidationUtils.lastNameInvalid(request.getLastName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Last name is invalid");
        }
        if (ValidationUtils.emailInvalid(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is invalid. Enter valid email address");
        } else if (authRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }
        if (ValidationUtils.passwordInvalid(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password is invalid. Password must be at least 8 characters long");
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
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(
                jwtService.generateToken(user.getEmail(), user.getRole().toString()),
                token.getRefreshToken(),
                user.getRole().toString()
        ));
    }

    @Override
    public ResponseEntity<?> login(LoginRequest request) {
        if (ValidationUtils.emailInvalid(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is invalid");
        }
        Optional<User> user = authRepository.findByEmail(request.getEmail());
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid email");
        }
        if (ValidationUtils.passwordInvalid(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password is invalid");
        } else if (!passwordEncoder.matches(request.getPassword(), user.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
        }

        Token token = Token.builder()
                .refreshToken(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusDays(60))
                .user(user.get())
                .build();
        user.get().setToken(token);

        authRepository.save(user.get());
        return ResponseEntity.status(HttpStatus.OK).body(new AuthResponse(
                jwtService.generateToken(user.get().getEmail(), user.get().getRole().toString()),
                token.getRefreshToken(),
                user.get().getRole().toString()
        ));
    }

    @Override
    public ResponseEntity<?> forgotPassword(ForgotRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<?> resetPassword(ResetRequest request) {
        return null;
    }
}
