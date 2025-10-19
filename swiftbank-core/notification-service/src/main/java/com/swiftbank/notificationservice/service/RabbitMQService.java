package com.swiftbank.notificationservice.service;

import com.swiftbank.notificationservice.config.RabbitMQConfig;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RabbitMQService {
    private final SendService sendService;

    @RabbitListener(queues = RabbitMQConfig.RESET_QUEUE_NAME)
    public void receiveMessage(String message) {
        sendService.forgotPassword(message);
    }
}
