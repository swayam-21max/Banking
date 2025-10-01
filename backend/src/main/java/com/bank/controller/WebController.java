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

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User user = bankService.getUserByEmail(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("accounts", user.getAccounts());
        return "dashboard";
    }

    @PostMapping("/account/create")
    public String createAccount(Principal principal) {
        User user = bankService.getUserByEmail(principal.getName());
        bankService.createAccount(user);
        return "redirect:/dashboard";
    }

    @GetMapping("/deposit")
    public String showDepositForm(@RequestParam("accountId") Long accountId, Model model) {
        model.addAttribute("accountId", accountId);
        return "deposit";
    }

    @PostMapping("/deposit")
    public String processDeposit(@RequestParam("accountId") Long accountId,
                                 @RequestParam("amount") BigDecimal amount,
                                 Principal principal,
                                 Model model) {
        try {
            User user = bankService.getUserByEmail(principal.getName());
            boolean isOwner = user.getAccounts().stream().anyMatch(acc -> acc.getId().equals(accountId));
            if (!isOwner) {
                throw new IllegalAccessException("Access Denied.");
            }
            bankService.deposit(accountId, amount);
            model.addAttribute("message", "Deposit successful!");
        } catch (Exception e) {
            model.addAttribute("error", "Deposit failed: " + e.getMessage());
        }
        model.addAttribute("accountId", accountId);
        return "deposit";
    }

    @GetMapping("/withdraw")
    public String showWithdrawForm(@RequestParam("accountId") Long accountId, Model model) {
        model.addAttribute("accountId", accountId);
        return "withdraw";
    }

    @PostMapping("/withdraw")
    public String processWithdraw(@RequestParam("accountId") Long accountId,
                                  @RequestParam("amount") BigDecimal amount,
                                  Principal principal,
                                  Model model) {
        try {
            User user = bankService.getUserByEmail(principal.getName());
            boolean isOwner = user.getAccounts().stream().anyMatch(acc -> acc.getId().equals(accountId));
            if (!isOwner) {
                throw new IllegalAccessException("Access Denied.");
            }
            bankService.withdraw(accountId, amount);
            model.addAttribute("message", "Withdrawal successful!");
        } catch (Exception e) {
            model.addAttribute("error", "Withdrawal failed: " + e.getMessage());
        }
        model.addAttribute("accountId", accountId);
        return "withdraw";
    }
    
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

    @GetMapping("/transactions")
    public String transactions(@RequestParam("accountId") Long accountId, Model model, Principal principal) {
        User user = bankService.getUserByEmail(principal.getName());
        boolean isOwner = user.getAccounts().stream().anyMatch(acc -> acc.getId().equals(accountId));
        if (!isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }

        List<TransactionEntity> transactions = bankService.getTransactions(accountId);
        model.addAttribute("transactions", transactions);
        model.addAttribute("accountId", accountId);
        return "transactions";
    }
    
    @GetMapping("/transfer")
    public String showTransferForm(@RequestParam("fromAccountId") Long fromAccountId, Model model, Principal principal) {
        User user = bankService.getUserByEmail(principal.getName());
        boolean isOwner = user.getAccounts().stream().anyMatch(acc -> acc.getId().equals(fromAccountId));
        if (!isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }
        model.addAttribute("fromAccountId", fromAccountId);
        return "transfer";
    }

    @PostMapping("/transfer")
    public String processTransfer(@RequestParam("fromAccountId") Long fromAccountId,
                                  @RequestParam("toAccountId") Long toAccountId,
                                  @RequestParam("amount") BigDecimal amount,
                                  Principal principal,
                                  Model model) {
        try {
            User user = bankService.getUserByEmail(principal.getName());
            boolean isOwner = user.getAccounts().stream().anyMatch(acc -> acc.getId().equals(fromAccountId));
            if (!isOwner) {
                throw new IllegalAccessException("You do not have permission to transfer from this account.");
            }

            bankService.transfer(fromAccountId, toAccountId, amount);
            model.addAttribute("message", "Transfer successful!");
        } catch (Exception e) {
            model.addAttribute("error", "Transfer failed: " + e.getMessage());
        }
        model.addAttribute("fromAccountId", fromAccountId);
        return "transfer";
    }
}

