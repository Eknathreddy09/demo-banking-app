package com.imaginelanka.bank.repository;

import com.imaginelanka.bank.domain.Account;
import com.imaginelanka.bank.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserOrderById(User user);
    Optional<Account> findByAccountNumber(String accountNumber);
}
