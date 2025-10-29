package com.swiftbank.accountservice.service.impl;

import com.swiftbank.accountservice.dto.*;
import com.swiftbank.accountservice.exceptions.CreateAccountException;
import com.swiftbank.accountservice.exceptions.InsufficientFundsException;
import com.swiftbank.accountservice.models.Account;
import com.swiftbank.accountservice.models.enumModels.Currency;
import com.swiftbank.accountservice.repository.AccountRepository;
import com.swiftbank.accountservice.service.AccountService;
import com.swiftbank.accountservice.service.RabbitMQServiceProducer;
import com.swiftbank.accountservice.utils.IBANUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final RabbitMQServiceProducer rabbitMQServiceProducer;
    @Value("${country.code}")
    private String COUNTRY_CODE;
    @Value("${bank.code}")
    private String BANK_CODE;

    @Override
    public void createAccountForNewUser(UserRegisteredPayload payload) {
        if (
                payload == null ||
                payload.getUserId() == null ||
                payload.getCreatedAt() == null
        ) {
            throw new CreateAccountException("invalid payload");
        }
        if (accountRepository.existsAccountByUserId(payload.getUserId())){
            throw new CreateAccountException("invalid user id");
        }
        Account account = Account.builder()
                .userId(payload.getUserId())
                .number(IBANUtils.generateIBAN(COUNTRY_CODE, BANK_CODE, accountRepository))
                .balance(BigDecimal.ZERO)
                .currency(Currency.UAH)
                .active(true)
                .createdAt(payload.getCreatedAt())
                .build();
        accountRepository.save(account);

        rabbitMQServiceProducer.sendMessage(
                AccountCreatedPayload.builder()
                        .accountNumber(account.getNumber())
                        .userId(account.getUserId())
                        .build()
        );
    }

    @Override
    public ResponseEntity<?> getAccountsByUserId(Long userId) {
        Optional<List<Account>> accountOptional = accountRepository.findAccountsByUserId(userId);
        if (accountOptional.isEmpty() || accountOptional.get().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Account> accounts = accountOptional.get();
        List<AccountResponse> accountResponseList = new ArrayList<>();
        accounts.forEach(account -> {
            AccountResponse accountResponse = AccountResponse.builder()
                    .balance(account.getBalance())
                    .currency(account.getCurrency().toString())
                    .build();
            accountResponseList.add(accountResponse);
        });
        return ResponseEntity.ok(accountResponseList);
    }

    @Override
    public ResponseEntity<?> getBalance(GetBalanceRequest request) {
        Optional<Account> account = accountRepository.findAccountByNumber(request.getAccountNumber());
        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(account.get().getBalance());
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
