package com.swiftbank.notificationservice.service;

import com.swiftbank.notificationservice.config.RabbitMQConfig;
import com.swiftbank.notificationservice.dto.ResetPayload;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RabbitMQService {
    private final SendService sendService;

    @RabbitListener(queues = RabbitMQConfig.RESET_QUEUE_NAME)
    public void receiveMessage(ResetPayload message) {
        sendService.forgotPassword(message);
    }
}
