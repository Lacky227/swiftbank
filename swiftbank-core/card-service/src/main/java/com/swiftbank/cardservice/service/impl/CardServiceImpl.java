package com.swiftbank.cardservice.service.impl;

import com.swiftbank.cardservice.dto.AccountCreatePayload;
import com.swiftbank.cardservice.dto.dto.CardResponse;
import com.swiftbank.cardservice.exceptions.CreateCardException;
import com.swiftbank.cardservice.models.Card;
import com.swiftbank.cardservice.repository.CardRepository;
import com.swiftbank.cardservice.service.CardService;
import com.swiftbank.cardservice.utils.CardUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    @Override
    public void createCardForNewAccount(AccountCreatePayload payload) {
        if (payload == null || payload.getAccountNumber() == null || payload.getAccountNumber().isEmpty()) {
            throw new CreateCardException("Error payload is null or empty");
        }
        Card card = Card.builder()
                .accountNumber(payload.getAccountNumber())
                .userId(payload.getUserId())
                .number(CardUtils.generateCardNumber())
                .expiryMonth(LocalDate.now().plusMonths(4).getMonthValue())
                .expiryYear(LocalDate.now().plusYears(4).getYear())
                .build();
        cardRepository.save(card);
    }

    @Override
    public ResponseEntity<?> getCardByUserIdAndAccountNumber(Long userId, String accountNumber) {
        Optional<Card> foundCard = cardRepository.findByUserIdAndAccountNumber(userId, accountNumber);
        if (foundCard.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(
                CardResponse.builder()
                        .number(foundCard.get().getNumber())
                        .expiryMonth(foundCard.get().getExpiryMonth())
                        .expiryYear(foundCard.get().getExpiryYear())
                        .build()
        );
    }
}
