package com.swiftbank.cardservice.service;

import com.swiftbank.cardservice.config.RabbitMQConfig;
import com.swiftbank.cardservice.dto.AccountCreatePayload;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RabbitMQServiceConsumer {
    private final CardService cardService;
    @RabbitListener(queues = RabbitMQConfig.QUEUE_ACCOUNT_CREATED)
    private void createdCard(AccountCreatePayload accountCreatePayload) {
        cardService.createCardForNewAccount(accountCreatePayload);
    }
}
