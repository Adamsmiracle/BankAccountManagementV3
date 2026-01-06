package com.miracle.src.services;

import com.miracle.src.models.Account;
import com.miracle.src.models.Transaction;
import com.miracle.src.utils.FileIOUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Service class for handling file persistence operations.
 * This class provides a clean interface for saving and loading
 * accounts and transactions to/from files using NIO.
 *
 * Delegates actual file I/O operations to FileIOUtils.
 */
public class FilePersistenceService {

    private static final FilePersistenceService INSTANCE = new FilePersistenceService();

    private FilePersistenceService() {
    }

    /**
     * Gets the singleton instance of FilePersistenceService.
     * @return the singleton instance
     */
    public static FilePersistenceService getInstance() {
        return INSTANCE;
    }

    /**
     * Saves accounts to the accounts.txt file.
     * @param accounts Map of account number to Account objects to save
     */
    public void saveAccounts(Map<String, Account> accounts) {
        FileIOUtils.saveAccountsToFile(accounts);
    }

    /**
     * Loads accounts from the accounts.txt file.
     * @return List of account lines loaded from file
     */
    public List<String> loadAccounts() {
        return FileIOUtils.readAccountsFromFile();
    }

    /**
     * Saves transactions to the transactions.txt file.
     * @param transactions List of transactions to save
     */
    public void saveTransactions(List<Transaction> transactions) {
        FileIOUtils.saveTransactionsToFile(transactions);
    }

    /**
     * Loads transactions from the transactions.txt file.
     * @return List of Transaction objects loaded from file
     */
    public List<Transaction> loadTransactions() {
        return FileIOUtils.readTransactionsFromFile();
    }

    /**
     * Loads all data (accounts and transactions) on application startup.
     * @throws IOException if file reading fails
     */
    public void loadAllDataOnStartup() throws IOException {
        System.out.println("Loading data from files...");
        loadAccounts();
        loadTransactions();
        System.out.println("Data loading complete.");
    }

    /**
     * Saves all data (accounts and transactions) on application exit.
     */
    public void saveAllDataOnExit() {
        System.out.println("Saving data to files...");
        AccountManager.getInstance().saveAccountsOnExit();
        TransactionManager.getInstance().saveTransactionsOnExit();
        System.out.println("Data saving complete.");
    }
}

