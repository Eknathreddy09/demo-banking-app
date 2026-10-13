package com.imaginelanka.bank.repository;

import com.imaginelanka.bank.domain.Notification;
import com.imaginelanka.bank.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
}
