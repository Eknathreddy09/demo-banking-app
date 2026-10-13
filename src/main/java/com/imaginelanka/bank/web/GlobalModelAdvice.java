package com.imaginelanka.bank.web;

import com.imaginelanka.bank.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalModelAdvice {

    private final CurrentUserProvider currentUserProvider;

    public GlobalModelAdvice(CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    @ModelAttribute("currentUserName")
    public String currentUserName(Principal principal) {
        if (principal == null) {
            return null;
        }
        try {
            User user = currentUserProvider.require(principal);
            return user.getFullName();
        } catch (Exception ex) {
            return null;
        }
    }

    @ModelAttribute("activePage")
    public String activePage(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String[] parts = uri.split("/");
        return parts.length > 1 ? parts[1] : "dashboard";
    }
}
