package com.swiftbank.cardservice.service.impl;

import com.swiftbank.cardservice.dto.AccountCreatePayload;
import com.swiftbank.cardservice.exceptions.CreateCardException;
import com.swiftbank.cardservice.models.Card;
import com.swiftbank.cardservice.repository.CardRepository;
import com.swiftbank.cardservice.service.CardService;
import com.swiftbank.cardservice.utils.CardUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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
                .IBAN(payload.getAccountNumber())
                .userId(payload.getUserId())
                .number(CardUtils.generateCardNumber())
                .expiryMonth(LocalDate.now().plusMonths(4).getMonthValue())
                .expiryYear(LocalDate.now().plusYears(4).getYear())
                .build();
        cardRepository.save(card);
    }
}
