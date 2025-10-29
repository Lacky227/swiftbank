package com.swiftbank.authservice.service;

import com.swiftbank.authservice.config.RabbitMQConfig;
import com.swiftbank.authservice.dto.UserRegisteredPayload;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RabbitMQService {
    private final RabbitTemplate rabbitTemplate;

    public void sendResetPassword(Object message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.RESET_ROUTING_KEY,
                message);
    }
    public void sendCreateAccount(UserRegisteredPayload payload) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.REGISTER_ROUTING_KEY,
                payload
        );
    }
}
