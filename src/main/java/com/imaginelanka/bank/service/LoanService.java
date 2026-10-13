package com.imaginelanka.bank.service;

import com.imaginelanka.bank.domain.*;
import com.imaginelanka.bank.messaging.TransactionEventPublisher;
import com.imaginelanka.bank.messaging.events.BankingEvent;
import com.imaginelanka.bank.repository.AccountRepository;
import com.imaginelanka.bank.repository.LoanRepository;
import com.imaginelanka.bank.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class LoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanService.class);

    private static final Map<String, BigDecimal> DEFAULT_RATES = Map.of(
            "PERSONAL", new BigDecimal("12.5"),
            "HOME", new BigDecimal("8.5"),
            "AUTO", new BigDecimal("10.0")
    );

    private final LoanRepository loanRepository;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionEventPublisher eventPublisher;

    public LoanService(LoanRepository loanRepository, AccountService accountService, AccountRepository accountRepository,
                        TransactionRepository transactionRepository, TransactionEventPublisher eventPublisher) {
        this.loanRepository = loanRepository;
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<Loan> getLoansForUser(User user) {
        return loanRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public void applyForLoan(User user, Long accountId, String loanType, BigDecimal principal, int termMonths) {
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Loan amount must be greater than zero");
        }
        Account account = accountService.requireOwnedAccount(user, accountId);

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setAccount(account);
        loan.setLoanType(loanType);
        loan.setPrincipal(principal);
        loan.setInterestRate(DEFAULT_RATES.getOrDefault(loanType, new BigDecimal("11.0")));
        loan.setTermMonths(termMonths);
        loan.setOutstandingBalance(principal);
        loan.setStatus("PENDING");
        loanRepository.save(loan);

        eventPublisher.publish(new BankingEvent(
                "LOAN_APPLIED",
                user.getId(),
                "Loan application received",
                String.format("Your %s loan application for %s %s is under review.", loanType,
                        account.getCurrency(), principal.toPlainString())
        ));
    }

    @Transactional
    public void payInstallment(User user, Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));
        if (!loan.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Loan does not belong to the current user");
        }
        if (!"APPROVED".equals(loan.getStatus())) {
            throw new IllegalArgumentException("Loan is not active");
        }

        Account account = loan.getAccount();
        BigDecimal payment = loan.getMonthlyPayment().min(loan.getOutstandingBalance());
        if (account.getBalance().compareTo(payment) < 0) {
            throw new IllegalArgumentException("Insufficient funds in " + account.getNickname());
        }

        account.setBalance(account.getBalance().subtract(payment));
        accountRepository.save(account);

        loan.setOutstandingBalance(loan.getOutstandingBalance().subtract(payment));
        if (loan.getOutstandingBalance().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setOutstandingBalance(BigDecimal.ZERO);
            loan.setStatus("CLOSED");
        }
        loanRepository.save(loan);

        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setType(TransactionType.LOAN_PAYMENT);
        tx.setAmount(payment.negate());
        tx.setDescription(loan.getLoanType() + " loan installment");
        tx.setCounterparty("Imagine Lanka Bank Loans");
        transactionRepository.save(tx);

        eventPublisher.publish(new BankingEvent(
                "LOAN_PAYMENT",
                user.getId(),
                "Loan installment paid",
                String.format("Installment of %s %s applied to your %s loan.", account.getCurrency(),
                        payment.toPlainString(), loan.getLoanType())
        ));
    }

    @Scheduled(fixedDelay = 15000, initialDelay = 15000)
    @Transactional
    public void autoApprovePendingLoans() {
        List<Loan> pending = loanRepository.findAll().stream()
                .filter(l -> "PENDING".equals(l.getStatus()))
                .toList();

        for (Loan loan : pending) {
            Account account = loan.getAccount();
            account.setBalance(account.getBalance().add(loan.getPrincipal()));
            accountRepository.save(account);

            loan.setStatus("APPROVED");
            loanRepository.save(loan);

            Transaction tx = new Transaction();
            tx.setAccount(account);
            tx.setType(TransactionType.LOAN_DISBURSEMENT);
            tx.setAmount(loan.getPrincipal());
            tx.setDescription(loan.getLoanType() + " loan disbursement");
            tx.setCounterparty("Imagine Lanka Bank Loans");
            transactionRepository.save(tx);

            eventPublisher.publish(new BankingEvent(
                    "LOAN_APPROVED",
                    loan.getUser().getId(),
                    "Loan approved",
                    String.format("Your %s loan for %s %s has been approved and disbursed to %s.",
                            loan.getLoanType(), account.getCurrency(), loan.getPrincipal().toPlainString(),
                            account.getNickname())
            ));

            log.info("Auto-approved loan {} for user {}", loan.getId(), loan.getUser().getId());
        }
    }
}
