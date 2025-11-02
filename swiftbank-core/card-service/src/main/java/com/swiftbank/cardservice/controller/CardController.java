package com.swiftbank.cardservice.controller;

import com.swiftbank.cardservice.service.CardService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/card")
@AllArgsConstructor
public class CardController {
    private CardService cardService;

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<?> getCard(@RequestHeader("X-User-Id") Long userId, @PathVariable String accountNumber) {
        return cardService.getCardByUserIdAndAccountNumber(userId, accountNumber);
    }

}
