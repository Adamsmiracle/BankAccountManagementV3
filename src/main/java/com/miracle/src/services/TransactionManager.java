package com.miracle.src.services;

import com.miracle.src.models.Transaction;
import com.miracle.src.utils.FileIOUtils;
import com.miracle.src.utils.FunctionalUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.CopyOnWriteArrayList;

public class TransactionManager {
    private static final TransactionManager INSTANCE = new TransactionManager();
    private static final List<Transaction> transactions = new ArrayList<>();
    private static final List<Transaction> newTransactions = new CopyOnWriteArrayList<>();

    public static TransactionManager getInstance() {
        return INSTANCE;
    }

    private TransactionManager() {}



    // Transaction Storage Methods
    public void addTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
            transactions.add(transaction);
            newTransactions.add(transaction);
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


    public void sortTransactionsByDate() {
        FunctionalUtils.sortTransactionsByDate(transactions);
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
            transactions.clear();
            newTransactions.clear();
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

    public static List<Transaction> getAllTransactions() {
        return transactions;
    }

    public void saveTransactionsOnExit() {
        try {
            if (newTransactions.isEmpty()) {
                System.out.println("No new transactions to save.");
                return;
            }
            // Take a snapshot to minimize lock contention while writing to disk
            List<Transaction> snapshot;
                snapshot = new ArrayList<>(newTransactions);
                newTransactions.clear();
            FileIOUtils.appendTransactionsToFile(snapshot);
        } catch (Exception e) {
            System.err.println(" error saving transactions: " + e.getMessage());
            e.printStackTrace();
        }
    }
}