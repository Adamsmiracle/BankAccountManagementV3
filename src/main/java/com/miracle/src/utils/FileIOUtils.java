package com.miracle.src.utils;

import com.miracle.src.dto.AccountRequest;
import com.miracle.src.models.*;
import com.miracle.src.models.exceptions.InvalidAmountException;
import com.miracle.src.services.AccountManager;
import com.miracle.src.services.TransactionManager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.*;
import java.util.stream.Collectors;

import static com.miracle.src.models.Transaction.TIMESTAMP_FORMATTER;

public class FileIOUtils {
    private static final String DATA_DIR = "src/main/java/com/miracle/data";

    private static final AccountManager accountManager = AccountManager.getInstance();


    private static final String ACCOUNTS_FILE_NAME = "accounts.txt";
    private static final String TRANSACTIONS_FILE_NAME = "transactions.txt";


    private static final Path accountFile = Paths.get(DATA_DIR, ACCOUNTS_FILE_NAME);
    private static final Path transactionFile = Paths.get(DATA_DIR, TRANSACTIONS_FILE_NAME);


    public static void saveAccountToFile(Map<String, Account> accounts) {
        List<String> lines = serializeAccounts(accounts);
        Path backupFile = Paths.get(DATA_DIR, ACCOUNTS_FILE_NAME + ".bak");
        
        try {
            if (Files.exists(accountFile)) {
                Files.copy(accountFile, backupFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Write new data
            Files.write(
                    accountFile,
                    lines,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
            System.out.println("Successfully saved " + lines.size() + " accounts.");
            
            if (Files.exists(backupFile)) {
                Files.delete(backupFile);
            }
        } catch (IOException e) {
            System.err.println("Failed to write accounts to file: " + e.getMessage());
            
            // Attempt to restore from backup
            try {
                if (Files.exists(backupFile)) {
                    Files.copy(backupFile, accountFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    System.err.println("Restored accounts from backup.");
                }
            } catch (IOException restoreError) {
                System.err.println("Failed to restore backup: " + restoreError.getMessage());
            }
        }
    }

    // Append only the provided accounts to the accounts file without touching existing data
    public static void appendAccountsToFile(Map<String, Account> accountsToAppend) {
        if (accountsToAppend == null || accountsToAppend.isEmpty()) {
            System.out.println("No new accounts to save.");
            return;
        }

        List<String> lines = serializeAccounts(accountsToAppend);
        try {
            Files.write(
                    accountFile,
                    lines,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
            System.out.println("Successfully saved " + lines.size() + " new account" + (lines.size() == 1 ? "." : "s."));
        } catch (IOException e) {
            System.err.println("Failed to append accounts to file: " + e.getMessage());
        }
    }


    public static List<String> readAccountsFromFile() {
        if (Files.notExists(accountFile)) {
            System.err.println("Account file does not exist: " + accountFile);
            return List.of();
        }

        List<String> loadedAccounts = new ArrayList<>();
        try {
            // Get existing account numbers for quick lookup
            Set<String> existingAccountNumbers = new HashSet<>(
                    accountManager.getAllAccounts().stream()
                            .map(Account::getAccountNumber)
                            .collect(Collectors.toSet())
            );

            // Read and process lines
            List<String> lines = Files.readAllLines(accountFile).stream()
                    .filter(line -> !line.trim().isEmpty())
                    .toList();

            int skippedCount = 0;
            int loadedCount = 0;

            for (String line : lines) {
                String accountNumber = extractAccountNumber(line);
                if (accountNumber != null) {
                    if (existingAccountNumbers.contains(accountNumber)) {
                        skippedCount++;
                        continue;
                    }
                    parseAccount(line);
                    loadedAccounts.add(line);
                    loadedCount++;
                }
            }

            // Update account count
            int totalAccounts = accountManager.getAccountCount();
            accountManager.setAccountCount(new AtomicInteger(totalAccounts + loadedCount));

            System.out.printf(loadedCount + " accounts loaded Successfully from accounts.txt.\n");

        } catch (IOException e) {
            System.err.println("Failed to load accounts from file: " + e.getMessage());
        }
        return loadedAccounts;
    }

    // Helper method to extract account number from a line
    private static String extractAccountNumber(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split("\\|");
        return (parts.length > 0) ? parts[0].trim() : null;
    }


    public static void writeToTransaction() {
        try {
            ensureDataDirExists();
            if (Files.notExists(transactionFile)) {
                Files.createFile(transactionFile);
            }
        } catch (IOException ignored) {
        }
    }

    // Persist a single transaction entry to the transactions file
    public static void SaveTransactionToFile(Transaction txn) {
        if (txn == null) return;
        String line = serializeTransaction(txn);
        try {
            ensureDataDirExists();
            Files.write(
                    transactionFile,
                    List.of(line),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.err.println("Failed to append transaction: " + e.getMessage());
        }
    }



    // Factory to rebuild a transaction from a serialized line
    public static Transaction deserializeTransaction(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        String[] parts = line.split("\\|");
        if (parts.length < 6) return null;
        String id = parts[0].trim();
        String accNo = parts[1].trim();
        String type = parts[2].trim();
        double amount = Double.parseDouble(parts[3].trim());
        double balanceAfter = Double.parseDouble(parts[4].trim());
        LocalDateTime ts = LocalDateTime.parse(parts[5].trim(), TIMESTAMP_FORMATTER);
        return new Transaction(id, accNo, type, amount, balanceAfter, ts);
    }

    public static List<Transaction> readTransactionsFromFile() {
        List<Transaction> newTransactions = new ArrayList<>();
        try {
            ensureDataDirExists();

            if (Files.notExists(transactionFile)) {
                System.out.println("No transaction file found at: " + transactionFile);
                return newTransactions;
            }

            // Get existing transactions for comparison
            List<Transaction> existingTransactions = TransactionManager.getInstance().getAllTransactions();
            Set<Transaction> existingTransactionSet = new HashSet<>(existingTransactions);

            // Read and process lines
            List<String> lines;
            try {
                lines = Files.readAllLines(transactionFile);
            } catch (IOException e) {
                System.err.println("Error reading transaction file: " + e.getMessage());
                return newTransactions;
            }

            int loadedCount = 0;
            int duplicateCount = 0;
            int errorCount = 0;

            for (String line : lines) {
                if (line == null || line.trim().isEmpty()) {
                    continue;
                }

                try {
                    Transaction t = deserializeTransaction(line.trim());
                    if (t != null && t.getTransactionId() != null) {
                        // Check if an equivalent transaction already exists
                        if (!existingTransactionSet.contains(t)) {
                            newTransactions.add(t);
                            existingTransactionSet.add(t);
                            loadedCount++;
                        } else {
                            duplicateCount++;
                        }
                    }
                } catch (Exception e) {
                    errorCount++;
                    System.err.println("Skipping invalid transaction: " + line);
                    System.err.println("Error: " + e.getMessage());
                }
            }

            // Log summary
            if (loadedCount > 0 || duplicateCount > 0 || errorCount > 0) {
                System.out.println( loadedCount + " transactions loaded from transactions.txt: ");
                if (duplicateCount > 0) {
                    System.out.println("  - Skipped duplicates: " + duplicateCount);
                }
                if (errorCount > 0) {
                    System.out.println("  - Errors encountered: " + errorCount);
                }
            }

        } catch (Exception e) {
            System.err.println("Fatal error loading transactions: " + e.getMessage());
            e.printStackTrace();
        }
        return newTransactions;
    }

    // Append multiple transactions (e.g., only new ones) to the file
    public static void saveTransactionsToFile(List<Transaction> txns) {
        if (txns == null || txns.isEmpty()) return;
        List<String> lines = txns.stream()
                .map(FileIOUtils::serializeTransaction)
                .collect(Collectors.toList());
        try {
            ensureDataDirExists();
            Files.write(
                    transactionFile,
                    lines,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
            System.out.println("Successfully saved " + lines.size() + " new transaction" + (lines.size()==1?"":"s") + ".");
        } catch (IOException e) {
            System.err.println("Failed to append transactions: " + e.getMessage());
        }
    }



    private static void ensureDataDirExists() throws IOException {
        Path dir = Paths.get(DATA_DIR);
        if (Files.notExists(dir)) {
            Files.createDirectories(dir);
        }
    }

    // Serialize a transaction into a pipe-delimited line compatible with file storage
    public static String serializeTransaction(Transaction t) {
        return String.join("|",
                t.getTransactionId(),
                t.getAccountNumber(),
                t.getType(),
                String.format("%.2f", t.getAmount()),
                String.format("%.2f", t.getBalanceAfter()),
                t.getFormattedTimestamp()
        );
    }


//    sE
    public static List<String> serializeAccounts(Map<String, Account> accounts) {
        return accounts.entrySet().stream()
                .map(entry -> {
                    String accountNumber = entry.getKey();
                    Account account = entry.getValue();
                    Customer customer = account.getCustomer();

                    return String.join("|",
                            accountNumber,
                            customer.getName(),
                            String.valueOf(customer.getAge()),
                            customer.getContact(),
                            customer.getAddress(),
                            customer.getCustomerId(),
                            customer.getCustomerType(),
                            account.getAccountType(),
                            String.format("%.2f", account.getBalance())
                    );
                })
                .toList();
    }


    public static void parseAccount(String line) {
        if (line == null || line.trim().isEmpty()) {
            System.err.println("Empty line in account file");
            return;
        }

        String[] column = line.split("\\|");

        try {
            int index = 0;
            String accountNumber = column[index++].trim();
            String customerName = column[index++].trim();
            int customerAge = Integer.parseInt(column[index++].trim());
            String customerContact = column[index++].trim();
            String customerAddress = column[index++].trim();

            // Determine if we have customer ID or if we're at customer type
            String customerId;
            String customerType;

            if (column.length == 9) {
                // New format with customer ID
                customerId = column[index++].trim();
                customerType = column[index++].trim();
            } else if (column.length == 8) {
                // Old format without customer ID
                customerId = "CUS" + String.format("%03d", Customer.customerCounter++);
                customerType = column[index++].trim();
            } else {
                System.err.println("Invalid account format: " + line);
                return;
            }

            String accountType = column[index++].trim();
            double balance = Double.parseDouble(column[index].trim());

            // Create the account request
            AccountRequest request = new AccountRequest(
                    customerName,
                    customerAge,
                    customerContact,
                    customerAddress,
                    "Regular".equalsIgnoreCase(customerType) ? 1 : 2,
                    "Savings".equalsIgnoreCase(accountType) ? 1 : 2,
                    balance
            );

            accountManager.createAccountFromFile(request);

        } catch (NumberFormatException e) {
            System.err.println("Invalid number format in account data: " + e.getMessage());
        } catch (InvalidAmountException e) {
            System.err.println("Invalid amount in account data: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error processing account: " + e.getMessage());
            e.printStackTrace();
        }
    }
}