package com.swiftbank.cardservice.service;

import com.swiftbank.cardservice.dto.AccountCreatePayload;
import org.springframework.http.ResponseEntity;

public interface CardService {
    void createCardForNewAccount(AccountCreatePayload payload);
    ResponseEntity<?> getCardByUserIdAndAccountNumber(Long userId, String accountNumber);
}
