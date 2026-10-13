package com.imaginelanka.bank.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "card")
@Getter
@Setter
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "card_number", nullable = false)
    private String cardNumber;

    @Column(name = "card_type", nullable = false)
    private String cardType;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(name = "expiry_month", nullable = false)
    private Integer expiryMonth;

    @Column(name = "expiry_year", nullable = false)
    private Integer expiryYear;

    @Column(name = "spending_limit")
    private BigDecimal spendingLimit;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getMaskedNumber() {
        String last4 = cardNumber.length() >= 4 ? cardNumber.substring(cardNumber.length() - 4) : cardNumber;
        return "•••• •••• •••• " + last4;
    }
}
