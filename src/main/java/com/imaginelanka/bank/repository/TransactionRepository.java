package com.imaginelanka.bank.repository;

import com.imaginelanka.bank.domain.Account;
import com.imaginelanka.bank.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountInOrderByCreatedAtDesc(List<Account> accounts);
    List<Transaction> findByAccountOrderByCreatedAtDesc(Account account);
}
