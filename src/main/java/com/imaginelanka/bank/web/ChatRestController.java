package com.imaginelanka.bank.web;

import com.imaginelanka.bank.domain.User;
import com.imaginelanka.bank.service.BankAssistantService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
public class ChatRestController {

    private final CurrentUserProvider currentUserProvider;
    private final BankAssistantService bankAssistantService;

    public ChatRestController(CurrentUserProvider currentUserProvider, BankAssistantService bankAssistantService) {
        this.currentUserProvider = currentUserProvider;
        this.bankAssistantService = bankAssistantService;
    }

    @PostMapping("/api/ai/chat")
    public Map<String, String> chat(Principal principal, @RequestBody Map<String, String> body) {
        User user = currentUserProvider.require(principal);
        String question = body.getOrDefault("message", "").trim();
        if (question.isEmpty()) {
            return Map.of("reply", "Ask me anything about your accounts, transactions, or recent activity.");
        }
        String reply = bankAssistantService.ask(user, question);
        return Map.of("reply", reply);
    }
}
