package com.swiftbank.authservice.service;

import com.swiftbank.authservice.dto.*;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<?> getUser(Long userId);
    ResponseEntity<?> register(RegisterRequest request);
    ResponseEntity<?> login(LoginRequest request);
    ResponseEntity<?> refresh(RefreshTokenRequest request);
    ResponseEntity<?> forgotPassword(ForgotRequest request);
    ResponseEntity<?> resetPassword(ResetRequest request);
}
