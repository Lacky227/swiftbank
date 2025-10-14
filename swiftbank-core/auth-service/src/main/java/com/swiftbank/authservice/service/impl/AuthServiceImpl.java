package com.swiftbank.authservice.service.impl;

import com.swiftbank.authservice.dto.ForgotRequest;
import com.swiftbank.authservice.dto.LoginRequest;
import com.swiftbank.authservice.dto.RegisterRequest;
import com.swiftbank.authservice.dto.ResetRequest;
import com.swiftbank.authservice.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public ResponseEntity<?> register(RegisterRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<?> login(LoginRequest request) {
        return null;
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
