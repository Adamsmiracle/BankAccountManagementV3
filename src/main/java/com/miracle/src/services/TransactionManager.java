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

        if (transactions.size() >= maxTransactions) {
            System.out.println("Transaction limit reached. Dropping transaction:");
        } else {
            transactions.add(transaction);
            newTransactions.add(transaction);
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
            FileIOUtils.saveTransactionsToFile(snapshot);
        } catch (Exception e) {
            System.err.println(" error saving transactions: " + e.getMessage());
            e.printStackTrace();
        }
    }
}