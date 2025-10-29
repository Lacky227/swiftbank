package com.swiftbank.accountservice.service;

import com.swiftbank.accountservice.config.RabbitMQConfig;
import com.swiftbank.accountservice.dto.AccountCreatedPayload;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RabbitMQServiceProducer {
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(AccountCreatedPayload payload) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.CREATED_ROUTING_KEY,
                payload);
    }
}
