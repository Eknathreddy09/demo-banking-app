package com.imaginelanka.bank.web;

import com.imaginelanka.bank.domain.User;
import com.imaginelanka.bank.service.AccountService;
import com.imaginelanka.bank.service.NotificationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class DashboardController {

    private final CurrentUserProvider currentUserProvider;
    private final AccountService accountService;
    private final NotificationService notificationService;

    public DashboardController(CurrentUserProvider currentUserProvider, AccountService accountService,
                                NotificationService notificationService) {
        this.currentUserProvider = currentUserProvider;
        this.accountService = accountService;
        this.notificationService = notificationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        User user = currentUserProvider.require(principal);
        model.addAttribute("accounts", accountService.getAccountsForUser(user));
        model.addAttribute("transactions", accountService.getRecentTransactions(user, 8));
        model.addAttribute("notificationCount", notificationService.getNotificationsForUser(user).size());
        return "dashboard";
    }
}
