package com.bank.controller;

import com.bank.model.Account;
import com.bank.model.TransactionEntity;
import com.bank.model.User;
import com.bank.service.BankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
public class WebController {

    @Autowired
    private BankService bankService;

    /**
     * Displays the main dashboard, showing a list of all the user's accounts.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User user = bankService.getUserByEmail(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("accounts", user.getAccounts());
        return "dashboard";
    }

    /**
     * Shows details for a specific account.
     * Includes a crucial security check to ensure the requested account belongs to the logged-in user.
     */
    @GetMapping("/account")
    public String accountDetails(@RequestParam("id") Long accountId, Model model, Principal principal) {
        User user = bankService.getUserByEmail(principal.getName());
        Account account = user.getAccounts().stream()
                .filter(acc -> acc.getId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found or access denied"));

        model.addAttribute("account", account);
        return "account";
    }

    /**
     * Displays the list of transactions for a specific, user-owned account.
     */
    @GetMapping("/transactions")
    public String transactions(@RequestParam("accountId") Long accountId, Model model, Principal principal) {
        User user = bankService.getUserByEmail(principal.getName());
        // Security Check: Verify the user owns this account before fetching transactions.
        boolean isOwner = user.getAccounts().stream().anyMatch(acc -> acc.getId().equals(accountId));
        if (!isOwner) {
            // Or redirect to an error page
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }

        List<TransactionEntity> transactions = bankService.getTransactions(accountId);
        model.addAttribute("transactions", transactions);
        model.addAttribute("accountId", accountId); // Pass accountId to the view
        return "transactions";
    }
    
    /**
     * Shows the transfer form.
     */
    @GetMapping("/transfer")
    public String showTransferForm(@RequestParam("fromAccountId") Long fromAccountId, Model model, Principal principal) {
        // Security check to ensure the "from" account belongs to the user
        User user = bankService.getUserByEmail(principal.getName());
        boolean isOwner = user.getAccounts().stream().anyMatch(acc -> acc.getId().equals(fromAccountId));
        if (!isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }
        model.addAttribute("fromAccountId", fromAccountId);
        return "transfer";
    }

    /**
     * Processes a fund transfer from one of the user's accounts to another account.
     */
    @PostMapping("/transfer")
    public String processTransfer(@RequestParam("fromAccountId") Long fromAccountId,
                                  @RequestParam("toAccountId") Long toAccountId,
                                  @RequestParam("amount") BigDecimal amount,
                                  Principal principal,
                                  Model model) {
        try {
            User user = bankService.getUserByEmail(principal.getName());
            // Security Check: Verify the user owns the source account.
            boolean isOwner = user.getAccounts().stream().anyMatch(acc -> acc.getId().equals(fromAccountId));
            if (!isOwner) {
                throw new IllegalAccessException("You do not have permission to transfer from this account.");
            }

            bankService.transfer(fromAccountId, toAccountId, amount);
            model.addAttribute("message", "Transfer successful!");
        } catch (Exception e) {
            model.addAttribute("error", "Transfer failed: " + e.getMessage());
        }
        // Return the user to the transfer form, showing the success/error message
        model.addAttribute("fromAccountId", fromAccountId);
        return "transfer";
    }
}