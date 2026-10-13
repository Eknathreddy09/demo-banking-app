package com.imaginelanka.bank.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EVENTS_EXCHANGE = "banking.events";
    public static final String NOTIFICATIONS_QUEUE = "banking.notifications.queue";
    public static final String ROUTING_KEY_PATTERN = "banking.event.#";

    @Bean
    public TopicExchange bankingEventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationsQueue() {
        return new Queue(NOTIFICATIONS_QUEUE, true);
    }

    @Bean
    public Binding notificationsBinding(Queue notificationsQueue, TopicExchange bankingEventsExchange) {
        return BindingBuilder.bind(notificationsQueue).to(bankingEventsExchange).with(ROUTING_KEY_PATTERN);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
