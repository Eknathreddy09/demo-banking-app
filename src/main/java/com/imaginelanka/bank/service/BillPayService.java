package com.imaginelanka.bank.service;

import com.imaginelanka.bank.domain.*;
import com.imaginelanka.bank.messaging.TransactionEventPublisher;
import com.imaginelanka.bank.messaging.events.BankingEvent;
import com.imaginelanka.bank.repository.AccountRepository;
import com.imaginelanka.bank.repository.BillPaymentRepository;
import com.imaginelanka.bank.repository.BillerRepository;
import com.imaginelanka.bank.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BillPayService {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final BillerRepository billerRepository;
    private final BillPaymentRepository billPaymentRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionEventPublisher eventPublisher;

    public BillPayService(AccountService accountService, AccountRepository accountRepository,
                           BillerRepository billerRepository, BillPaymentRepository billPaymentRepository,
                           TransactionRepository transactionRepository, TransactionEventPublisher eventPublisher) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.billerRepository = billerRepository;
        this.billPaymentRepository = billPaymentRepository;
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<Biller> getBillers() {
        return billerRepository.findAll();
    }

    @Transactional
    public void payBill(User user, Long accountId, Long billerId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        Account account = accountService.requireOwnedAccount(user, accountId);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds in " + account.getNickname());
        }

        Biller biller = billerRepository.findById(billerId)
                .orElseThrow(() -> new IllegalArgumentException("Biller not found"));

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setType(TransactionType.BILL_PAYMENT);
        tx.setAmount(amount.negate());
        tx.setDescription("Bill payment - " + biller.getName());
        tx.setCounterparty(biller.getName());
        transactionRepository.save(tx);

        BillPayment payment = new BillPayment();
        payment.setAccount(account);
        payment.setBiller(biller);
        payment.setAmount(amount);
        billPaymentRepository.save(payment);

        eventPublisher.publish(new BankingEvent(
                "BILL_PAYMENT",
                user.getId(),
                "Bill payment successful",
                String.format("Paid %s %s to %s from %s.", account.getCurrency(), amount.toPlainString(),
                        biller.getName(), account.getNickname())
        ));
    }
}
