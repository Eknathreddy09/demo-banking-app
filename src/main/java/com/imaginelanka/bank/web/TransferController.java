package com.imaginelanka.bank.web;

import com.imaginelanka.bank.domain.User;
import com.imaginelanka.bank.service.AccountService;
import com.imaginelanka.bank.service.TransferService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
public class TransferController {

    private final CurrentUserProvider currentUserProvider;
    private final AccountService accountService;
    private final TransferService transferService;

    public TransferController(CurrentUserProvider currentUserProvider, AccountService accountService,
                               TransferService transferService) {
        this.currentUserProvider = currentUserProvider;
        this.accountService = accountService;
        this.transferService = transferService;
    }

    @GetMapping("/transfer")
    public String form(Principal principal, Model model) {
        User user = currentUserProvider.require(principal);
        model.addAttribute("accounts", accountService.getAccountsForUser(user));
        return "transfer";
    }

    @PostMapping("/transfer")
    public String submit(Principal principal, @RequestParam Long fromAccountId, @RequestParam String toAccountNumber,
                          @RequestParam BigDecimal amount, @RequestParam(required = false) String note,
                          RedirectAttributes redirectAttributes) {
        User user = currentUserProvider.require(principal);
        try {
            transferService.transfer(user, fromAccountId, toAccountNumber.trim(), amount, note);
            redirectAttributes.addFlashAttribute("success", "Transfer completed successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/transfer";
    }
}
