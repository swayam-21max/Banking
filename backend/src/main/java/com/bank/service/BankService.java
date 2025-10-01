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

    public User registerUser(User user) {
        return userRepo.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email).orElse(null);
    }

    public Account createAccount(User user) {
        Account acc = new Account();
        acc.setBalance(BigDecimal.ZERO);
        acc.setUser(user);
        return accountRepo.save(acc);
    }

    @Transactional
    public void deposit(Long accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        Account acc = accountRepo.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));
        acc.setBalance(acc.getBalance().add(amount));
        accountRepo.save(acc);
        saveTransaction(acc, "DEPOSIT", amount);
    }

    @Transactional
    public void withdraw(Long accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        Account acc = accountRepo.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));
        if (acc.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds for withdrawal.");
        }
        acc.setBalance(acc.getBalance().subtract(amount));
        accountRepo.save(acc);
        saveTransaction(acc, "WITHDRAW", amount);
    }

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive.");
        }
        Account fromAcc = accountRepo.findById(fromId)
            .orElseThrow(() -> new RuntimeException("Source account not found with id: " + fromId));
        Account toAcc = accountRepo.findById(toId)
            .orElseThrow(() -> new RuntimeException("Destination account not found with id: " + toId));

        if (fromAcc.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds for transfer.");
        }

        fromAcc.setBalance(fromAcc.getBalance().subtract(amount));
        toAcc.setBalance(toAcc.getBalance().add(amount));

        accountRepo.save(fromAcc);
        accountRepo.save(toAcc);

        saveTransaction(fromAcc, "TRANSFER_OUT", amount);
        saveTransaction(toAcc, "TRANSFER_IN", amount);
    }

    public BigDecimal getBalance(Long accountId) {
        return accountRepo.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId))
            .getBalance();
    }

    public List<TransactionEntity> getTransactions(Long accountId) {
        return txRepo.findByAccount_Id(accountId);
    }

    private void saveTransaction(Account acc, String type, BigDecimal amount) {
        TransactionEntity tx = new TransactionEntity();
        tx.setAccount(acc);
        tx.setType(type);
        tx.setAmount(amount);
        tx.setTransactionDate(LocalDateTime.now()); // UPDATED
        tx.setBalanceAfterTransaction(acc.getBalance());
        txRepo.save(tx);
    }
}