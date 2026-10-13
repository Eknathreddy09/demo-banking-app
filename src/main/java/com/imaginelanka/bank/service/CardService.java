package com.imaginelanka.bank.service;

import com.imaginelanka.bank.domain.Account;
import com.imaginelanka.bank.domain.Card;
import com.imaginelanka.bank.domain.User;
import com.imaginelanka.bank.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final AccountService accountService;

    public CardService(CardRepository cardRepository, AccountService accountService) {
        this.cardRepository = cardRepository;
        this.accountService = accountService;
    }

    public List<Card> getCardsForUser(User user) {
        List<Account> accounts = accountService.getAccountsForUser(user);
        return cardRepository.findByAccountInOrderById(accounts);
    }

    @Transactional
    public void setBlocked(User user, Long cardId, boolean blocked) {
        Card card = requireOwnedCard(user, cardId);
        card.setStatus(blocked ? "BLOCKED" : "ACTIVE");
        cardRepository.save(card);
    }

    @Transactional
    public void requestNewCard(User user, Long accountId, String cardType) {
        Account account = accountService.requireOwnedAccount(user, accountId);

        Card card = new Card();
        card.setAccount(account);
        card.setCardNumber(generateCardNumber(cardType));
        card.setCardType(cardType);
        card.setExpiryMonth(ThreadLocalRandom.current().nextInt(1, 13));
        card.setExpiryYear(Year.now().getValue() + 4);
        card.setSpendingLimit("CREDIT".equals(cardType) ? new BigDecimal("200000.00") : new BigDecimal("100000.00"));
        cardRepository.save(card);
    }

    private Card requireOwnedCard(User user, Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));
        if (!card.getAccount().getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Card does not belong to the current user");
        }
        return card;
    }

    private String generateCardNumber(String cardType) {
        String prefix = "CREDIT".equals(cardType) ? "54" : "45";
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < 14; i++) {
            sb.append(ThreadLocalRandom.current().nextInt(0, 10));
        }
        return sb.toString();
    }
}
