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

    /**
     * Constructor for BankService.
     * The PasswordEncoder dependency has been removed to allow for plain-text passwords.
     */
    public BankService(UserRepository userRepo,
                       AccountRepository accountRepo,
                       TransactionRepository txRepo) {
        this.userRepo = userRepo;
        this.accountRepo = accountRepo;
        this.txRepo = txRepo;
    }

    /**
     * Registers a new user by saving their details to the database.
     * The user's password is saved as plain text.
     *
     * @param user The user object to be registered.
     * @return The saved User entity.
     */
    public User registerUser(User user) {
        // Password is saved as-is, without any encoding.
        return userRepo.save(user);
    }

    /**
     * Finds a user by their email address.
     *
     * @param email The email of the user to find.
     * @return The User object if found, otherwise null.
     */
    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email).orElse(null);
    }

    /**
     * Creates a new bank account for a given user with a zero balance.
     *
     * @param user The user for whom the account is being created.
     * @return The newly created Account entity.
     */
    public Account createAccount(User user) {
        Account acc = new Account();
        acc.setBalance(BigDecimal.ZERO);
        acc.setUser(user);
        return accountRepo.save(acc);
    }

    /**
     * Deposits a specified amount into an account.
     *
     * @param accountId The ID of the account to deposit into.
     * @param amount The amount to deposit. Must be positive.
     */
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

    /**
     * Withdraws a specified amount from an account.
     *
     * @param accountId The ID of the account to withdraw from.
     * @param amount The amount to withdraw. Must be positive and not exceed the balance.
     */
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

    /**
     * Transfers a specified amount between two accounts.
     *
     * @param fromId The ID of the source account.
     * @param toId The ID of the destination account.
     * @param amount The amount to transfer.
     */
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

    /**
     * Retrieves the balance of a specific account.
     *
     * @param accountId The ID of the account.
     * @return The account balance.
     */
    public BigDecimal getBalance(Long accountId) {
        return accountRepo.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId))
            .getBalance();
    }

    /**
     * Retrieves the transaction history for a specific account.
     *
     * @param accountId The ID of the account.
     * @return A list of transactions.
     */
    public List<TransactionEntity> getTransactions(Long accountId) {
        return txRepo.findByAccount_Id(accountId);
    }

    /**
     * A private helper method to create and save a transaction record.
     *
     * @param acc The account associated with the transaction.
     * @param type The type of transaction (e.g., DEPOSIT, WITHDRAW).
     * @param amount The amount of the transaction.
     */
    private void saveTransaction(Account acc, String type, BigDecimal amount) {
        TransactionEntity tx = new TransactionEntity();
        tx.setAccount(acc);
        tx.setType(type);
        tx.setAmount(amount);
        tx.setDate(LocalDateTime.now());
        txRepo.save(tx);
    }
}

