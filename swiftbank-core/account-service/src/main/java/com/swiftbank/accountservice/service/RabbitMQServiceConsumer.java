package com.swiftbank.accountservice.service;

import com.swiftbank.accountservice.config.RabbitMQConfig;
import com.swiftbank.accountservice.dto.UserRegisteredPayload;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RabbitMQServiceConsumer {
    private AccountService accountService;

    @RabbitListener(queues = RabbitMQConfig.REGISTER_QUEUE_NAME)
    private void registerAccount(UserRegisteredPayload payload) {
        accountService.createAccountForNewUser(payload);
    }
}
