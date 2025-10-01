package com.bank.service;

import com.bank.model.Account;
import com.bank.model.TransactionEntity;
import com.bank.model.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import com.bank.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BankService {

    private final UserRepository userRepo;
    private final AccountRepository accountRepo;
    private final TransactionRepository txRepo;

    public BankService(UserRepository userRepo,
                       AccountRepository accountRepo,
                       TransactionRepository txRepo) {
        this.userRepo = userRepo;
        this.accountRepo = accountRepo;
        this.txRepo = txRepo;
    }

    // Register
    public User registerUser(User user) {
        return userRepo.save(user);
    }

    // Find User by Email
    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email).orElse(null);
    }

    // Create Account
    public Account createAccount(User user) {
        Account acc = new Account();
        acc.setBalance(BigDecimal.ZERO);
        acc.setUser(user);
        return accountRepo.save(acc);
    }

    // Deposit
    @Transactional
    public void deposit(Long accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit must be positive");
        }

        Account acc = accountRepo.findById(accountId).orElseThrow();
        acc.setBalance(acc.getBalance().add(amount));
        accountRepo.save(acc);
        saveTransaction(acc, "DEPOSIT", amount);
    }

    // Withdraw
    @Transactional
    public void withdraw(Long accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal must be positive");
        }

        Account acc = accountRepo.findById(accountId).orElseThrow();

        if (acc.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds in account " + accountId);
        }

        acc.setBalance(acc.getBalance().subtract(amount));
        accountRepo.save(acc);
        saveTransaction(acc, "WITHDRAW", amount);
    }

    // Transfer
    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer must be positive");
        }

        Account fromAcc = accountRepo.findById(fromId).orElseThrow();
        Account toAcc = accountRepo.findById(toId).orElseThrow();

        if (fromAcc.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds in account " + fromId);
        }

        fromAcc.setBalance(fromAcc.getBalance().subtract(amount));
        toAcc.setBalance(toAcc.getBalance().add(amount));

        accountRepo.save(fromAcc);
        accountRepo.save(toAcc);

        saveTransaction(fromAcc, "TRANSFER_OUT", amount);
        saveTransaction(toAcc, "TRANSFER_IN", amount);
    }

    // Delete Account
    public void deleteAccount(Long accountId) {
        accountRepo.deleteById(accountId);
    }

    // Get Balance
    public BigDecimal getBalance(Long accountId) {
        return accountRepo.findById(accountId).orElseThrow().getBalance();
    }

    // Get Transactions
    public List<TransactionEntity> getTransactions(Long accountId) {
    return txRepo.findByAccount_Id(accountId);
}

    private void saveTransaction(Account acc, String type, BigDecimal amount) {
        TransactionEntity tx = new TransactionEntity();
        tx.setAccount(acc);
        tx.setType(type);
        tx.setAmount(amount);
        tx.setDate(LocalDateTime.now());
        txRepo.save(tx);
    }
}
