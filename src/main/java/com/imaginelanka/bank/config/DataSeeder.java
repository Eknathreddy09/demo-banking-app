package com.imaginelanka.bank.config;

import com.imaginelanka.bank.domain.*;
import com.imaginelanka.bank.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final LoanRepository loanRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${banking.demo.username}")
    private String demoUsername;

    @Value("${banking.demo.password}")
    private String demoPassword;

    public DataSeeder(UserRepository userRepository, AccountRepository accountRepository,
                       TransactionRepository transactionRepository, CardRepository cardRepository,
                       LoanRepository loanRepository, NotificationRepository notificationRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
        this.loanRepository = loanRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.findByUsername(demoUsername).isPresent()) {
            return;
        }

        User user = new User();
        user.setUsername(demoUsername);
        user.setPasswordHash(passwordEncoder.encode(demoPassword));
        user.setFullName("Kumara Sangakkar");
        user.setEmail("kumara.sangakkar@imaginelanka.lk");
        userRepository.save(user);

        Account savings = new Account();
        savings.setUser(user);
        savings.setAccountNumber("1000100012345");
        savings.setAccountType(AccountType.SAVINGS);
        savings.setNickname("Primary Savings");
        savings.setBalance(new BigDecimal("250000.00"));
        accountRepository.save(savings);

        Account checking = new Account();
        checking.setUser(user);
        checking.setAccountNumber("1000100067890");
        checking.setAccountType(AccountType.CHECKING);
        checking.setNickname("Everyday Checking");
        checking.setBalance(new BigDecimal("85000.00"));
        accountRepository.save(checking);

        seedTransaction(savings, TransactionType.DEPOSIT, new BigDecimal("250000.00"), "Opening deposit", "Imagine Lanka Bank", 10);
        seedTransaction(checking, TransactionType.DEPOSIT, new BigDecimal("100000.00"), "Salary credit", "Acme Holdings Pvt Ltd", 7);
        seedTransaction(checking, TransactionType.BILL_PAYMENT, new BigDecimal("-7500.00"), "Electricity bill payment", "Ceylon Electricity Board", 3);
        seedTransaction(checking, TransactionType.TRANSFER_OUT, new BigDecimal("-7500.00"), "Transfer to savings", "Primary Savings", 1);

        Card debitCard = new Card();
        debitCard.setAccount(checking);
        debitCard.setCardNumber("4556730012345678");
        debitCard.setCardType("DEBIT");
        debitCard.setExpiryMonth(11);
        debitCard.setExpiryYear(2029);
        debitCard.setSpendingLimit(new BigDecimal("150000.00"));
        cardRepository.save(debitCard);

        Card creditCard = new Card();
        creditCard.setAccount(savings);
        creditCard.setCardNumber("5412750098765432");
        creditCard.setCardType("CREDIT");
        creditCard.setExpiryMonth(4);
        creditCard.setExpiryYear(2028);
        creditCard.setSpendingLimit(new BigDecimal("300000.00"));
        cardRepository.save(creditCard);

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setAccount(savings);
        loan.setLoanType("PERSONAL");
        loan.setPrincipal(new BigDecimal("500000.00"));
        loan.setInterestRate(new BigDecimal("12.5"));
        loan.setTermMonths(24);
        loan.setOutstandingBalance(new BigDecimal("500000.00"));
        loan.setStatus("APPROVED");
        loanRepository.save(loan);

        Notification welcome = new Notification();
        welcome.setUser(user);
        welcome.setTitle("Welcome to Imagine Lanka Bank");
        welcome.setMessage("Your accounts are ready. Explore transfers, bill pay, loans and cards.");
        welcome.setEventType("SYSTEM");
        notificationRepository.save(welcome);
    }

    private void seedTransaction(Account account, TransactionType type, BigDecimal amount, String description,
                                  String counterparty, int daysAgo) {
        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setType(type);
        tx.setAmount(amount);
        tx.setDescription(description);
        tx.setCounterparty(counterparty);
        tx.setCreatedAt(LocalDateTime.now().minusDays(daysAgo));
        transactionRepository.save(tx);
    }
}
