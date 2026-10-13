package com.imaginelanka.bank.messaging.events;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BankingEvent implements Serializable {

    private String eventType;
    private Long userId;
    private String title;
    private String message;
    private LocalDateTime occurredAt;

    public BankingEvent() {
    }

    public BankingEvent(String eventType, Long userId, String title, String message) {
        this.eventType = eventType;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.occurredAt = LocalDateTime.now();
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}
