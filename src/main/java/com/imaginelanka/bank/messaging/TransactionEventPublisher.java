package com.imaginelanka.bank.messaging;

import com.imaginelanka.bank.config.RabbitConfig;
import com.imaginelanka.bank.messaging.events.BankingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public TransactionEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(BankingEvent event) {
        String routingKey = "banking.event." + event.getEventType().toLowerCase();
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EVENTS_EXCHANGE, routingKey, event);
            log.info("Published event [{}] for user {}", event.getEventType(), event.getUserId());
        } catch (Exception ex) {
            log.warn("Failed to publish event [{}] to RabbitMQ: {}", event.getEventType(), ex.getMessage());
        }
    }
}
