package com.swiftbank.authservice.service;

import com.swiftbank.authservice.dto.ForgotRequest;
import com.swiftbank.authservice.dto.LoginRequest;
import com.swiftbank.authservice.dto.RegisterRequest;
import com.swiftbank.authservice.dto.ResetRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<?> register(RegisterRequest request);
    ResponseEntity<?> login(LoginRequest request);
    ResponseEntity<?> forgotPassword(ForgotRequest request);
    ResponseEntity<?> resetPassword(ResetRequest request);
}
