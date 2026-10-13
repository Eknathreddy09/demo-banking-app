package com.imaginelanka.bank.messaging;

import com.imaginelanka.bank.config.RabbitConfig;
import com.imaginelanka.bank.domain.Notification;
import com.imaginelanka.bank.domain.User;
import com.imaginelanka.bank.messaging.events.BankingEvent;
import com.imaginelanka.bank.repository.NotificationRepository;
import com.imaginelanka.bank.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventConsumer.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public TransactionEventConsumer(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @RabbitListener(queues = RabbitConfig.NOTIFICATIONS_QUEUE)
    public void onBankingEvent(BankingEvent event) {
        log.info("Consumed event [{}] for user {}", event.getEventType(), event.getUserId());

        User user = userRepository.findById(event.getUserId()).orElse(null);
        if (user == null) {
            log.warn("Dropping event [{}]: unknown user {}", event.getEventType(), event.getUserId());
            return;
        }

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(event.getTitle());
        notification.setMessage(event.getMessage());
        notification.setEventType(event.getEventType());
        notificationRepository.save(notification);
    }
}
