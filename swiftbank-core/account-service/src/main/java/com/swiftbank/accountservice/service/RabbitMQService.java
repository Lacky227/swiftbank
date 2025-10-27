package com.swiftbank.accountservice.service;

import com.swiftbank.accountservice.config.RabbitMQConfig;
import com.swiftbank.accountservice.dto.AccountCreatedPayload;
import com.swiftbank.accountservice.dto.UserRegisteredPayload;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RabbitMQService {
    private AccountService accountService;
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.REGISTER_QUEUE_NAME)
    private void registerAccount(UserRegisteredPayload payload) {
        accountService.createAccountForNewUser(payload);
    }

    public void sendMessage(AccountCreatedPayload payload) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.CREATED_QUEUE_NAME,
                payload);
    }
}
