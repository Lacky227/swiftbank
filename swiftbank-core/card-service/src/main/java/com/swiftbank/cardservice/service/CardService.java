package com.swiftbank.cardservice.service;

import com.swiftbank.cardservice.dto.AccountCreatePayload;

public interface CardService {
    void createCardForNewAccount(AccountCreatePayload payload);
}
