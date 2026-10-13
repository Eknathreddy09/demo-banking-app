package com.imaginelanka.bank.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan")
@Getter
@Setter
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "loan_type", nullable = false)
    private String loanType;

    @Column(nullable = false)
    private BigDecimal principal;

    @Column(name = "interest_rate", nullable = false)
    private BigDecimal interestRate;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @Column(name = "outstanding_balance", nullable = false)
    private BigDecimal outstandingBalance;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public BigDecimal getMonthlyPayment() {
        if (termMonths == null || termMonths == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal rate = interestRate.divide(BigDecimal.valueOf(1200), 10, java.math.RoundingMode.HALF_UP);
        BigDecimal totalWithInterest = principal.add(principal.multiply(rate).multiply(BigDecimal.valueOf(termMonths)));
        return totalWithInterest.divide(BigDecimal.valueOf(termMonths), 2, java.math.RoundingMode.HALF_UP);
    }
}
