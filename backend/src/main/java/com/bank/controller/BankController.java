package com.bank.controller;

import com.bank.model.Account;
import com.bank.model.TransactionEntity;
import com.bank.model.User;
import com.bank.service.BankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class BankController {

    @Autowired
    private BankService bankService;

    // Register
    @PostMapping("/register")
    public Map<String, String> register(@RequestBody User user) {
        bankService.registerUser(user);
        return Map.of("message", "Registration successful");
    }

    // Login
    @PostMapping("/login")
    public Map<String, Object> login(Principal principal) {
        User user = bankService.getUserByEmail(principal.getName());
        Long activeAccountId = (user.getAccounts().isEmpty()) ? null : user.getAccounts().get(0).getId();
        return Map.of("message", "Login successful", "activeAccountId", activeAccountId);
    }

    // Create Account
    @PostMapping("/account/create")
    public Map<String, Object> createAccount(Principal principal) {
        User user = bankService.getUserByEmail(principal.getName());
        Account acc = bankService.createAccount(user);
        return Map.of("accountId", acc.getId());
    }

    // Deposit
    @PostMapping("/account/deposit")
    public Map<String, String> deposit(@RequestBody Map<String, String> body, Principal principal) {
        User user = bankService.getUserByEmail(principal.getName());
        Long accId = user.getAccounts().get(0).getId();
        BigDecimal amount = new BigDecimal(body.get("amount"));
        bankService.deposit(accId, amount);
        return Map.of("message", "Deposit successful");
    }

    // Withdraw
    @PostMapping("/account/withdraw")
    public Map<String, String> withdraw(@RequestBody Map<String, String> body, Principal principal) {
        User user = bankService.getUserByEmail(principal.getName());
        Long accId = user.getAccounts().get(0).getId();
        BigDecimal amount = new BigDecimal(body.get("amount"));
        bankService.withdraw(accId, amount);
        return Map.of("message", "Withdraw successful");
    }

    // Transfer
    @PostMapping("/account/transfer")
    public Map<String, String> transfer(@RequestBody Map<String, String> body, Principal principal) {
        User user = bankService.getUserByEmail(principal.getName());
        Long fromId = user.getAccounts().get(0).getId();
        Long toId = Long.valueOf(body.get("targetAccountId"));
        BigDecimal amount = new BigDecimal(body.get("amount"));
        bankService.transfer(fromId, toId, amount);
        return Map.of("message", "Transfer successful");
    }

    // Delete Account
    @DeleteMapping("/account/delete")
    public Map<String, String> deleteAccount(Principal principal) {
        User user = bankService.getUserByEmail(principal.getName());
        Long accId = user.getAccounts().get(0).getId();
        bankService.deleteAccount(accId);
        return Map.of("message", "Account deleted");
    }

    // Balance
    @GetMapping("/account/balance")
    public Map<String, Object> getBalance(Principal principal) {
        User user = bankService.getUserByEmail(principal.getName());
        Long accId = user.getAccounts().get(0).getId();
        BigDecimal balance = bankService.getBalance(accId);
        return Map.of("balance", balance);
    }

    // Transactions
    @GetMapping("/account/transactions")
    public Map<String, Object> getTransactions(Principal principal) {
        User user = bankService.getUserByEmail(principal.getName());
        Long accId = user.getAccounts().get(0).getId();
        List<TransactionEntity> transactions = bankService.getTransactions(accId);
        return Map.of("transactions", transactions);
    }
}
