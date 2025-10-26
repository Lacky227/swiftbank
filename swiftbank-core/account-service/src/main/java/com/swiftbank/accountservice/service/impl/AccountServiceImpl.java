package com.swiftbank.accountservice.service.impl;

import com.swiftbank.accountservice.dto.OperationFundsRequest;
import com.swiftbank.accountservice.dto.UserRegisteredPayload;
import com.swiftbank.accountservice.exceptions.InsufficientFundsException;
import com.swiftbank.accountservice.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {
    @Override
    public ResponseEntity<?> createAccountForNewUser(UserRegisteredPayload payload) {
        return null;
    }

    @Override
    public ResponseEntity<?> getAccountsByUserId(Long userId) {
        return null;
    }

    @Override
    public ResponseEntity<?> getBalance(String accountNumber) {
        return null;
    }

    @Override
    public ResponseEntity<?> debitAccount(OperationFundsRequest request) throws InsufficientFundsException {
        return null;
    }

    @Override
    public ResponseEntity<?> creditAccount(OperationFundsRequest request) {
        return null;
    }
}
