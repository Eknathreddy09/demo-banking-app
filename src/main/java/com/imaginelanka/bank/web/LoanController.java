package com.imaginelanka.bank.web;

import com.imaginelanka.bank.domain.User;
import com.imaginelanka.bank.service.AccountService;
import com.imaginelanka.bank.service.LoanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
public class LoanController {

    private final CurrentUserProvider currentUserProvider;
    private final AccountService accountService;
    private final LoanService loanService;

    public LoanController(CurrentUserProvider currentUserProvider, AccountService accountService, LoanService loanService) {
        this.currentUserProvider = currentUserProvider;
        this.accountService = accountService;
        this.loanService = loanService;
    }

    @GetMapping("/loans")
    public String view(Principal principal, Model model) {
        User user = currentUserProvider.require(principal);
        model.addAttribute("accounts", accountService.getAccountsForUser(user));
        model.addAttribute("loans", loanService.getLoansForUser(user));
        return "loans";
    }

    @PostMapping("/loans/apply")
    public String apply(Principal principal, @RequestParam Long accountId, @RequestParam String loanType,
                         @RequestParam BigDecimal principal2, @RequestParam int termMonths,
                         RedirectAttributes redirectAttributes) {
        User user = currentUserProvider.require(principal);
        try {
            loanService.applyForLoan(user, accountId, loanType, principal2, termMonths);
            redirectAttributes.addFlashAttribute("success",
                    "Loan application submitted. It will be reviewed automatically within a few seconds.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/loans";
    }

    @PostMapping("/loans/{id}/pay")
    public String pay(Principal principal, @PathVariable("id") Long loanId, RedirectAttributes redirectAttributes) {
        User user = currentUserProvider.require(principal);
        try {
            loanService.payInstallment(user, loanId);
            redirectAttributes.addFlashAttribute("success", "Loan installment paid successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/loans";
    }
}
