package com.imaginelanka.bank.web;

import com.imaginelanka.bank.domain.User;
import com.imaginelanka.bank.service.AccountService;
import com.imaginelanka.bank.service.BillPayService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
public class BillPayController {

    private final CurrentUserProvider currentUserProvider;
    private final AccountService accountService;
    private final BillPayService billPayService;

    public BillPayController(CurrentUserProvider currentUserProvider, AccountService accountService,
                              BillPayService billPayService) {
        this.currentUserProvider = currentUserProvider;
        this.accountService = accountService;
        this.billPayService = billPayService;
    }

    @GetMapping("/billpay")
    public String form(Principal principal, Model model) {
        User user = currentUserProvider.require(principal);
        model.addAttribute("accounts", accountService.getAccountsForUser(user));
        model.addAttribute("billers", billPayService.getBillers());
        return "billpay";
    }

    @PostMapping("/billpay")
    public String submit(Principal principal, @RequestParam Long accountId, @RequestParam Long billerId,
                          @RequestParam BigDecimal amount, RedirectAttributes redirectAttributes) {
        User user = currentUserProvider.require(principal);
        try {
            billPayService.payBill(user, accountId, billerId, amount);
            redirectAttributes.addFlashAttribute("success", "Bill payment submitted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/billpay";
    }
}
