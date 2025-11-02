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
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.EVENT_PASSWORD_RESET_REQUESTED,
                message);
    }
    public void sendCreateAccount(UserRegisteredPayload payload) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.EVENT_USER_REGISTERED,
                payload
        );
    }
}
