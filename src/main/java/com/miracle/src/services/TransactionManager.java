package com.miracle.src.services;

import com.miracle.src.models.Transaction;
import com.miracle.src.utils.FileIOUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.concurrent.CopyOnWriteArrayList;

public class TransactionManager {
    private static final TransactionManager INSTANCE = new TransactionManager();
    // Thread-safe storage for transactions. CopyOnWriteArrayList favors read-heavy access patterns.
    private static final List<Transaction> transactions = new CopyOnWriteArrayList<>();
    // Track only transactions created during this session (after load)
    private static final List<Transaction> newTransactions = new CopyOnWriteArrayList<>();
    private final Object txLock = new Object();

    public static TransactionManager getInstance() {
        return INSTANCE;
    }

    private TransactionManager() {}


    // TransactionContext-based transaction management removed.
    // Atomicity and consistency are now guaranteed by per-account synchronization
    // and thread-safe transaction storage operations in this manager.

    // Transaction Storage Methods
    public void addTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        // Ensure atomic append to both collections
        synchronized (txLock) {
            transactions.add(transaction);
            newTransactions.add(transaction);
        }
    }


    public List<Transaction> getTransactionsByAccount(String accountNumber) {
        return transactions.stream()
                .filter(Objects::nonNull)
                .filter(t -> t.getAccountNumber().equalsIgnoreCase(accountNumber))
                .toList();
    }

    public void sortTransactionsByAmount() {
        // Sorting requires a snapshot to avoid concurrent modification semantics surprises
        List<Transaction> snapshot = new ArrayList<>(transactions);
        snapshot.sort(Comparator.comparing(Transaction::getAmount));
        synchronized (txLock) {
            transactions.clear();
            transactions.addAll(snapshot);
        }
    }

    public void sortTransactionsByDate() {
        // Sort by timestamp, most recent first (descending)
        List<Transaction> snapshot = new ArrayList<>(transactions);
        snapshot.sort(Comparator.comparing(Transaction::getTimestamp).reversed());
        synchronized (txLock) {
            transactions.clear();
            transactions.addAll(snapshot);
        }
    }


    public Transaction getTransaction(int index) {
        if (index < 0 || index >= transactions.size()) {
            throw new IndexOutOfBoundsException("Invalid transaction index: " + index);
        }
        return transactions.get(index);
    }

    public int getTransactionCount() {
        return transactions.size();
    }

    // Utility method to support tests: clear all stored transactions
    public void clearTransactions() {
        synchronized (txLock) {
            transactions.clear();
            newTransactions.clear();
        }
    }

    // Lifecycle persistence: load on start, save on exit
    public static void loadTransactionsOnStart() {
        try {
            List<Transaction> loaded = FileIOUtils.readTransactionsFromFile();
            transactions.clear();
            transactions.addAll(loaded);
            newTransactions.clear();
        } catch (Exception e) {
            System.err.println("Failed to load transactions: " + e.getMessage());
        }
    }

    public void saveTransactionsOnExit() {
        try {
            if (newTransactions.isEmpty()) {
                System.out.println("No new transactions to save.");
                return;
            }
            // Take a snapshot to minimize lock contention while writing to disk
            List<Transaction> snapshot;
            synchronized (txLock) {
                snapshot = new ArrayList<>(newTransactions);
                newTransactions.clear();
            }
            FileIOUtils.appendTransactionsToFile(snapshot);
        } catch (Exception e) {
            System.err.println("Critical error saving transactions: " + e.getMessage());
            e.printStackTrace();
        }
    }
}