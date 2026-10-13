package com.imaginelanka.bank.repository;

import com.imaginelanka.bank.domain.Account;
import com.imaginelanka.bank.domain.BillPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillPaymentRepository extends JpaRepository<BillPayment, Long> {
    List<BillPayment> findByAccountInOrderByCreatedAtDesc(List<Account> accounts);
}
