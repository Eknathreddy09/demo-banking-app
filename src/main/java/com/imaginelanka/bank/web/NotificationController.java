package com.imaginelanka.bank.web;

import com.imaginelanka.bank.domain.User;
import com.imaginelanka.bank.service.NotificationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class NotificationController {

    private final CurrentUserProvider currentUserProvider;
    private final NotificationService notificationService;

    public NotificationController(CurrentUserProvider currentUserProvider, NotificationService notificationService) {
        this.currentUserProvider = currentUserProvider;
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public String view(Principal principal, Model model) {
        User user = currentUserProvider.require(principal);
        model.addAttribute("notifications", notificationService.getNotificationsForUser(user));
        return "notifications";
    }
}
