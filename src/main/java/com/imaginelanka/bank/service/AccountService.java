package com.imaginelanka.bank.service;

import com.imaginelanka.bank.domain.Account;
import com.imaginelanka.bank.domain.User;
import com.imaginelanka.bank.repository.AccountRepository;
import com.imaginelanka.bank.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<Account> getAccountsForUser(User user) {
        return accountRepository.findByUserOrderById(user);
    }

    public Account requireOwnedAccount(User user, Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (!account.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Account does not belong to the current user");
        }
        return account;
    }

    public List<com.imaginelanka.bank.domain.Transaction> getRecentTransactions(User user, int limit) {
        List<Account> accounts = getAccountsForUser(user);
        List<com.imaginelanka.bank.domain.Transaction> all = transactionRepository.findByAccountInOrderByCreatedAtDesc(accounts);
        return all.size() > limit ? all.subList(0, limit) : all;
    }
}
