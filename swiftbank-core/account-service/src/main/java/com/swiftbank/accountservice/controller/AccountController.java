package com.swiftbank.accountservice.controller;

import com.swiftbank.accountservice.dto.GetBalanceRequest;
import com.swiftbank.accountservice.service.AccountService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
@AllArgsConstructor
public class AccountController {
    private AccountService accountService;

    @GetMapping("/user")
    ResponseEntity<?> getAccountsByUserId(@RequestHeader("X-User-Id") Long userId) {
        return accountService.getAccountsByUserId(userId);
    }
    @GetMapping("/balance")
    ResponseEntity<?> getBalance(@RequestBody GetBalanceRequest request) {
        return accountService.getBalance(request);
    }
}
