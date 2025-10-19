package com.swiftbank.notificationservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String SUBSCRIPTION_QUEUE_NAME = "auth-to-notif";
    public static final String RESET_QUEUE_NAME = "reset-password-queue";
    public static final String EXCHANGE_NAME = "auth-exchange";
    public static final String SUBSCRIPTION_ROUTING_KEY = "auth.to.notif";
    public static final String RESET_ROUTING_KEY = "auth.to.reset";

    @Bean
    public Queue subscriptionQueue() {
        return new Queue(SUBSCRIPTION_QUEUE_NAME, true);
    }

    @Bean
    public Queue resetQueue() {
        return new Queue(RESET_QUEUE_NAME, true);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding subscriptionBinding(Queue subscriptionQueue, DirectExchange exchange) {
        return BindingBuilder.bind(subscriptionQueue).to(exchange).with(SUBSCRIPTION_ROUTING_KEY);
    }

    @Bean
    public Binding resetBinding(Queue resetQueue, DirectExchange exchange) {
        return BindingBuilder.bind(resetQueue).to(exchange).with(RESET_ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }
}
