package com.imaginelanka.bank.service;

import com.imaginelanka.bank.domain.Notification;
import com.imaginelanka.bank.domain.User;
import com.imaginelanka.bank.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getNotificationsForUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }
}
