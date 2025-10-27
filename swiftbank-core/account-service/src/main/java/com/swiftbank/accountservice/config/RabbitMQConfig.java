package com.swiftbank.accountservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String REGISTER_QUEUE_NAME = "register-queue";
    public static final String CREATED_QUEUE_NAME = "created-queue";
    public static final String EXCHANGE_NAME = "auth-exchange";
    public static final String REGISTER_ROUTING_KEY = "auth.to.account";
    public static final String CREATED_ROUTING_KEY = "account.to.card";

    @Bean
    public Queue registerQueue() {
        return new Queue(REGISTER_QUEUE_NAME, true);
    }
    @Bean
    public Queue createdQueue() {
        return new Queue(CREATED_QUEUE_NAME, true);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding registerBinding(Queue registerQueue, DirectExchange exchange) {
        return BindingBuilder.bind(registerQueue).to(exchange).with(REGISTER_ROUTING_KEY);
    }
    @Bean
    public Binding createdBinding(Queue createdQueue, DirectExchange exchange) {
        return BindingBuilder.bind(createdQueue).to(exchange).with(CREATED_ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
