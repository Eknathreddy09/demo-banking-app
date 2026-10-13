package com.imaginelanka.bank.web;

import com.imaginelanka.bank.domain.User;
import com.imaginelanka.bank.service.AccountService;
import com.imaginelanka.bank.service.CardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class CardController {

    private final CurrentUserProvider currentUserProvider;
    private final AccountService accountService;
    private final CardService cardService;

    public CardController(CurrentUserProvider currentUserProvider, AccountService accountService, CardService cardService) {
        this.currentUserProvider = currentUserProvider;
        this.accountService = accountService;
        this.cardService = cardService;
    }

    @GetMapping("/cards")
    public String view(Principal principal, Model model) {
        User user = currentUserProvider.require(principal);
        model.addAttribute("cards", cardService.getCardsForUser(user));
        model.addAttribute("accounts", accountService.getAccountsForUser(user));
        return "cards";
    }

    @PostMapping("/cards/{id}/block")
    public String block(Principal principal, @PathVariable("id") Long cardId, RedirectAttributes redirectAttributes) {
        User user = currentUserProvider.require(principal);
        try {
            cardService.setBlocked(user, cardId, true);
            redirectAttributes.addFlashAttribute("success", "Card blocked.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cards";
    }

    @PostMapping("/cards/{id}/unblock")
    public String unblock(Principal principal, @PathVariable("id") Long cardId, RedirectAttributes redirectAttributes) {
        User user = currentUserProvider.require(principal);
        try {
            cardService.setBlocked(user, cardId, false);
            redirectAttributes.addFlashAttribute("success", "Card unblocked.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cards";
    }

    @PostMapping("/cards/request")
    public String request(Principal principal, @RequestParam Long accountId, @RequestParam String cardType,
                           RedirectAttributes redirectAttributes) {
        User user = currentUserProvider.require(principal);
        try {
            cardService.requestNewCard(user, accountId, cardType);
            redirectAttributes.addFlashAttribute("success", "New card requested successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cards";
    }
}
