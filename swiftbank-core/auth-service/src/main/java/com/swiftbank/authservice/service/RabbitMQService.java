package com.swiftbank.authservice.service;

import com.swiftbank.authservice.config.RabbitMQConfig;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RabbitMQService {
    private final RabbitTemplate rabbitTemplate;

    public void sendResetPassword(String message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.RESET_ROUTING_KEY,
                message);
    }
}
