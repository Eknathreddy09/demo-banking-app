package com.imaginelanka.bank.service;

import com.imaginelanka.bank.domain.*;
import com.imaginelanka.bank.messaging.TransactionEventPublisher;
import com.imaginelanka.bank.messaging.events.BankingEvent;
import com.imaginelanka.bank.repository.AccountRepository;
import com.imaginelanka.bank.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransferService {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionEventPublisher eventPublisher;

    public TransferService(AccountService accountService, AccountRepository accountRepository,
                            TransactionRepository transactionRepository, TransactionEventPublisher eventPublisher) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void transfer(User user, Long fromAccountId, String toAccountNumber, BigDecimal amount, String note) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }

        Account fromAccount = accountService.requireOwnedAccount(user, fromAccountId);
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds in " + fromAccount.getNickname());
        }
        if (toAccountNumber.equals(fromAccount.getAccountNumber())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        accountRepository.save(fromAccount);

        String description = (note == null || note.isBlank()) ? "Funds transfer" : note;
        Transaction debit = new Transaction();
        debit.setAccount(fromAccount);
        debit.setType(TransactionType.TRANSFER_OUT);
        debit.setAmount(amount.negate());
        debit.setDescription(description);
        debit.setCounterparty(toAccountNumber);
        transactionRepository.save(debit);

        var internalDestination = accountRepository.findByAccountNumber(toAccountNumber);
        if (internalDestination.isPresent()) {
            Account toAccount = internalDestination.get();
            toAccount.setBalance(toAccount.getBalance().add(amount));
            accountRepository.save(toAccount);

            Transaction credit = new Transaction();
            credit.setAccount(toAccount);
            credit.setType(TransactionType.TRANSFER_IN);
            credit.setAmount(amount);
            credit.setDescription(description);
            credit.setCounterparty(fromAccount.getAccountNumber());
            transactionRepository.save(credit);
        }

        eventPublisher.publish(new BankingEvent(
                "TRANSFER",
                user.getId(),
                "Transfer completed",
                String.format("%s %s transferred from %s to %s.", fromAccount.getCurrency(), amount.toPlainString(),
                        fromAccount.getNickname(), toAccountNumber)
        ));
    }
}
