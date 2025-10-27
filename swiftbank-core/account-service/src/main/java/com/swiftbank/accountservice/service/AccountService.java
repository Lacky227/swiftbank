package com.swiftbank.accountservice.service;

import com.swiftbank.accountservice.dto.OperationFundsRequest;
import com.swiftbank.accountservice.dto.UserRegisteredPayload;
import com.swiftbank.accountservice.exceptions.InsufficientFundsException;
import org.springframework.http.ResponseEntity;

public interface AccountService {
    void createAccountForNewUser(UserRegisteredPayload payload);
    ResponseEntity<?> getAccountsByUserId(Long userId);
    ResponseEntity<?> getBalance(String accountNumber);
    ResponseEntity<?> debitAccount(OperationFundsRequest request) throws InsufficientFundsException;
    ResponseEntity<?> creditAccount(OperationFundsRequest request);
}
