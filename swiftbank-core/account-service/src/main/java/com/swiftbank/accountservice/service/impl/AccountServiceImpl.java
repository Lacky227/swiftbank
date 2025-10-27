package com.swiftbank.accountservice.service.impl;

import com.swiftbank.accountservice.dto.AccountCreatedPayload;
import com.swiftbank.accountservice.dto.OperationFundsRequest;
import com.swiftbank.accountservice.dto.UserRegisteredPayload;
import com.swiftbank.accountservice.exceptions.CreateAccountException;
import com.swiftbank.accountservice.exceptions.InsufficientFundsException;
import com.swiftbank.accountservice.models.Account;
import com.swiftbank.accountservice.models.enumModels.Currency;
import com.swiftbank.accountservice.repository.AccountRepository;
import com.swiftbank.accountservice.service.AccountService;
import com.swiftbank.accountservice.service.RabbitMQService;
import com.swiftbank.accountservice.utils.IBANUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final RabbitMQService rabbitMQService;
    private final PasswordEncoder passwordEncoder;
    @Value("${country.code}")
    private String COUNTRY_CODE;
    @Value("${bank.code}")
    private String BANK_CODE;

    @Override
    public void createAccountForNewUser(UserRegisteredPayload payload) {
        if (
                payload == null ||
                payload.getUserId().describeConstable().isEmpty() ||
                payload.getCreatedAt() == null
        ) {
            throw new CreateAccountException("invalid payload");
        }
        if (accountRepository.existsAccountByUserId(payload.getUserId())){
            throw new CreateAccountException("invalid user id");
        }
        Account account = Account.builder()
                .userId(payload.getUserId())
                .number(passwordEncoder.encode(
                        IBANUtils.generateIBAN(COUNTRY_CODE, BANK_CODE, accountRepository)))
                .balance(BigDecimal.ZERO)
                .currency(Currency.UAH)
                .active(true)
                .createdAt(payload.getCreatedAt())
                .build();
        accountRepository.save(account);

        rabbitMQService.sendMessage(
                AccountCreatedPayload.builder()
                        .accountNumber(account.getNumber())
                        .build()
        );
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
