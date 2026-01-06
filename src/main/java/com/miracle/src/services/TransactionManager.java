package com.miracle.src.services;

import com.miracle.src.models.Transaction;
import com.miracle.src.utils.FileIOUtils;
import com.miracle.src.utils.FunctionalUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.CopyOnWriteArrayList;

public class TransactionManager {
    private static final TransactionManager INSTANCE = new TransactionManager();
    private static final int maxTransactions = 200;
    private static final List<Transaction> transactions = Collections.synchronizedList(new ArrayList<>());
    private static final List<Transaction> newTransactions = new CopyOnWriteArrayList<>();
    private static volatile boolean dataLoaded = false;

    public static TransactionManager getInstance() {
        return INSTANCE;
    }

    private TransactionManager() {}



    // Transaction Storage Methods
    public void addTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }

        if (transactions.size() >= maxTransactions) {
            System.out.println("Transaction limit reached. Dropping transaction:");
        } else {
            synchronized (transactions) {
                transactions.add(transaction);
            }
            // Only track as new if we've already loaded from file
            // This prevents loaded transactions from being re-saved
            if (dataLoaded) {
                newTransactions.add(transaction);
            }
        }
    }

    /**
     * Adds a transaction that was loaded from file (won't be tracked as new)
     */
    public void addLoadedTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        if (transactions.size() >= maxTransactions) {
            System.out.println("Transaction limit reached. Cannot load more transactions.");
        } else {
            synchronized (transactions) {
                transactions.add(transaction);
            }
        }
    }


    public static List<Transaction> filterTransactionsByType(String type) {
        if (transactions == null || type == null) {
            return Collections.emptyList();
        }

        String normalizedType = type.trim().toUpperCase();
        return transactions.stream()
                .filter(t -> t != null && t.getType() != null)
                .filter(t -> t.getType().toUpperCase().equals(normalizedType))
                .collect(Collectors.toList());
    }


    public List<Transaction> getTransactionsByAccount(String accountNumber) {
        return transactions.stream()
                .filter(Objects::nonNull)
                .filter(t -> t.getAccountNumber().equalsIgnoreCase(accountNumber))
                .toList();
    }


    public void sortTransactionsByAmount() {
        FunctionalUtils.sortTransactionsByAmount(transactions);
    }


    public List<Transaction> sortTransactionsByDate() {
        return FunctionalUtils.sortTransactionsByDate(transactions);
    }


    public List<Transaction> sorTransactionsByID(){
        return FunctionalUtils.sortTransactionsByIdDescending(transactions);
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


    /**
     * Groups transactions by their type (DEPOSIT, WITHDRAWAL, TRANSFER)
     * @return A Map where the key is the transaction type and the value is a list of transactions of that type
     */
    public Map<String, List<Transaction>> groupTransactionsByType() {
        return transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getType));
    }

    // Lifecycle persistence: load on start, save on exit
    public static void loadTransactionsOnStart() {
        try {
            List<Transaction> loaded = FileIOUtils.readTransactionsFromFile();

            // Only add transactions that were actually loaded (not already in memory)
            if (!loaded.isEmpty()) {
                synchronized (transactions) {
                    for (Transaction t : loaded) {
                        INSTANCE.addLoadedTransaction(t);
                    }
                }
            }

            newTransactions.clear(); // Clear any accidentally added new transactions
            dataLoaded = true; // Mark that loading is complete
            System.out.println("Transaction data loaded. Total transactions in memory: " + transactions.size());
        } catch (Exception e) {
            System.err.println("Failed to load transactions: " + e.getMessage());
            dataLoaded = true; // Still set to true so new transactions get tracked
        }
    }

    public  List<Transaction> getAllTransactions() {
        return transactions;
    }

    public void saveTransactionsOnExit() {
        try {
            if (newTransactions.isEmpty()) {
                System.out.println("No new transactions to save.");
                return;
            }
            // Take a snapshot to minimize lock contention while writing to disk
            List<Transaction> snapshot = new ArrayList<>(newTransactions);
            FileIOUtils.saveTransactionsToFile(snapshot);
            // Only clear after successful save
            newTransactions.clear();
        } catch (Exception e) {
            System.err.println(" error saving transactions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets the count of new transactions that will be saved on exit
     */
    public int getNewTransactionCount() {
        return newTransactions.size();
    }
}