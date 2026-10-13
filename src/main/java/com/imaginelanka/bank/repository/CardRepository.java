package com.imaginelanka.bank.repository;

import com.imaginelanka.bank.domain.Account;
import com.imaginelanka.bank.domain.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    @Query("select c from Card c join fetch c.account where c.account in :accounts order by c.id")
    List<Card> findByAccountInOrderById(@Param("accounts") List<Account> accounts);
}
