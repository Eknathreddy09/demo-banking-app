package com.imaginelanka.bank.repository;

import com.imaginelanka.bank.domain.Loan;
import com.imaginelanka.bank.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUserOrderByCreatedAtDesc(User user);
}
