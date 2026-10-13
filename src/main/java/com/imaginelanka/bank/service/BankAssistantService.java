package com.imaginelanka.bank.service;

import com.imaginelanka.bank.domain.Account;
import com.imaginelanka.bank.domain.Transaction;
import com.imaginelanka.bank.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BankAssistantService {

    private static final Logger log = LoggerFactory.getLogger(BankAssistantService.class);

    private static final String FALLBACK_MESSAGE =
            "I'm temporarily unable to reach the Imagine Lanka Bank AI assistant. " +
            "Make sure the 'ai-models' service is bound to this app (cf bind-service) and restarted, " +
            "then try again.";

    private final ChatClient chatClient;
    private final AccountService accountService;

    public BankAssistantService(ChatClient chatClient, AccountService accountService) {
        this.chatClient = chatClient;
        this.accountService = accountService;
    }

    public String ask(User user, String question) {
        try {
            String context = buildAccountContext(user);
            String systemPrompt = """
                    You are the Imagine Lanka Bank AI Assistant, a friendly and concise banking helper.
                    Answer only using the account information provided below. Keep responses short (2-4 sentences).
                    Never invent account numbers or balances that are not listed.

                    Customer: %s
                    %s
                    """.formatted(user.getFullName(), context);

            return chatClient.prompt()
                    .system(systemPrompt)
                    .user(question)
                    .call()
                    .content();
        } catch (Exception ex) {
            log.warn("AI assistant call failed: {}", ex.getMessage());
            return FALLBACK_MESSAGE;
        }
    }

    private String buildAccountContext(User user) {
        List<Account> accounts = accountService.getAccountsForUser(user);
        String accountsSummary = accounts.stream()
                .map(a -> "- %s (%s, %s): balance %s %s".formatted(
                        a.getNickname(), a.getAccountType(), a.getAccountNumber(),
                        a.getCurrency(), a.getBalance().toPlainString()))
                .collect(Collectors.joining("\n"));

        List<Transaction> recent = accountService.getRecentTransactions(user, 5);
        String recentSummary = recent.stream()
                .map(t -> "- %s: %s %s (%s)".formatted(
                        t.getCreatedAt().toLocalDate(), t.getType(), t.getAmount().toPlainString(), t.getDescription()))
                .collect(Collectors.joining("\n"));

        return "Accounts:\n" + accountsSummary + "\n\nRecent transactions:\n" + recentSummary;
    }
}
