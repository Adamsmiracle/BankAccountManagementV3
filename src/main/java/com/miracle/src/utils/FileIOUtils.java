package com.miracle.src.utils;

import com.miracle.src.models.*;
import com.miracle.src.services.AccountManager;
import com.miracle.src.services.TransactionManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.miracle.src.models.Transaction.TIMESTAMP_FORMATTER;

/**
 * The type File io utils.
 */
public class FileIOUtils {
    private static final String DATA_DIR = "src/main/java/com/miracle/data";

    private static final AccountManager accountManager = AccountManager.getInstance();


    private static final String ACCOUNTS_FILE_NAME = "accounts.txt";
    private static final String TRANSACTIONS_FILE_NAME = "transactions.txt";


    private static final Path accountFile = Paths.get(DATA_DIR, ACCOUNTS_FILE_NAME);
    private static final Path transactionFile = Paths.get(DATA_DIR, TRANSACTIONS_FILE_NAME);


    /**
     * Save accounts to file.
     *
     * @param accountsToAppend the accounts to append
     */
    public static void saveAccountsToFile(Map<String, Account> accountsToAppend) {
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


    /**
     * Read accounts from file list.
     *
     * @return the list
     */
    public static List<String> readAccountsFromFile() {
        if (Files.notExists(accountFile)) {
            System.err.println("Account file does not exist: " + accountFile);
            return List.of();
        }

        List<String> loadedAccounts = new ArrayList<>();
        try {
            // Get existing account numbers for quick lookup
            Set<String> existingAccountNumbers = accountManager.getAllAccounts().stream()
                    .map(Account::getAccountNumber).collect(Collectors.toSet());

            // Read and process lines
            List<String> lines = Files.readAllLines(accountFile).stream()
                    .filter(line -> !line.trim().isEmpty())
                    .toList();

            int loadedCount = 0;

            for (String line : lines) {
                String accountNumber = extractAccountNumber(line);
                if (accountNumber != null) {
                    if (existingAccountNumbers.contains(accountNumber)) {
                        continue;
                    }
                    parseAccount(line);
                    loadedAccounts.add(line);
                    loadedCount++;
                }
            }

            System.out.printf(loadedCount + " accounts loaded successfully from accounts.txt.\n");

        } catch (IOException e) {
            System.err.println("Failed to load accounts from file: " + e.getMessage());
        }
        return loadedAccounts;
    }





    private static String extractAccountNumber(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split("\\|");
        return (parts.length > 0) ? parts[0].trim() : null;
    }


    /**
     * Deserialize transactions.
     *
     * @param line the line
     * @return the transaction
     */
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

    /**
     * Read transactions from file.
     *
     * @return the list
     */
    public static List<Transaction> readTransactionsFromFile() {
        List<Transaction> loadedTransactions = new ArrayList<>();
        try {
            ensureDataDirExists();

            if (Files.notExists(transactionFile)) {
                System.out.println("No transaction file found at: " + transactionFile);
                return loadedTransactions;
            }

            // Read and process lines
            List<String> lines = Files.readAllLines(transactionFile)
                    .stream()
                    .filter(line -> !line.trim().isEmpty())
                    .toList();

            // Track IDs to avoid duplicates - include both file and in-memory transactions
            Set<String> existingIds = new HashSet<>();

            // Add IDs of transactions already in memory
            TransactionManager.getInstance().getAllTransactions().stream()
                    .filter(t -> t != null && t.getTransactionId() != null)
                    .forEach(t -> existingIds.add(t.getTransactionId()));

            int skippedCount = 0;

            for (String line : lines) {
                try {
                    Transaction t = deserializeTransaction(line.trim());
                    if (t != null && t.getTransactionId() != null) {
                        // Only add if not already in memory or already loaded from file
                        if (!existingIds.contains(t.getTransactionId())) {
                            loadedTransactions.add(t);
                            existingIds.add(t.getTransactionId());
                        } else {
                            skippedCount++;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Skipping invalid transaction: " + line);
                    System.err.println("Error: " + e.getMessage());
                }
            }

            // Log summary
                System.out.println(loadedTransactions.size() + " transactions loaded from transactions.txt");

        } catch (Exception e) {
            System.err.println("Fatal error loading transactions: " + e.getMessage());
            e.printStackTrace();
        }
        return loadedTransactions;
    }

    /**
     * Save transactions to file.
     *
     * @param txns the txns
     */
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

    /**
     * Serialize transaction string.
     *
     * @param t the t
     * @return the string
     */
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


    /**
     * Serialize accounts list.
     *
     * @param accounts the accounts
     * @return the list
     */
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


    /**
     * Parse account.
     *
     * @param line the line
     */
    public static void parseAccount(String line) {
        if (line == null || line.trim().isEmpty()) {
            System.err.println("Empty line in account file");
            return;
        }

        String[] columns = line.split("\\|");
        if (columns.length < 9) {
            System.err.println("Invalid account format. Expected 9 fields but got " + columns.length);
            return;
        }

        try {
            int index = 0;
            String accountNumber = columns[index++].trim();
            String customerName = columns[index++].trim();
            int customerAge = Integer.parseInt(columns[index++].trim());
            String customerContact = columns[index++].trim();
            String customerAddress = columns[index++].trim();
            String customerId = columns[index++].trim();
            String customerType = columns[index++].trim();
            String accountType = columns[index++].trim();
            double balance = Double.parseDouble(columns[index].trim());

            // Create customer using the file-loading constructor (preserves customer ID)
            Customer customer;
            if ("Regular".equalsIgnoreCase(customerType)) {
                customer = new RegularCustomer(customerName, customerAge, customerContact, customerAddress, customerId, true);
            } else {
                customer = new PremiumCustomer(customerName, customerAge, customerContact, customerAddress, customerId, true);
            }

            // Create account using the file-loading constructor (preserves account number, no transaction)
            Account account;
            if ("Savings".equalsIgnoreCase(accountType)) {
                account = new SavingsAccount(customer, balance, accountNumber, true);
            } else {
                account = new CheckingAccount(customer, balance, accountNumber, true);
            }

            // Add to account manager without tracking as "newly created"
            accountManager.addAccountFromFile(account);

        } catch (NumberFormatException e) {
            System.err.println("Invalid number format in account data: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error processing account: " + e.getMessage());
            e.printStackTrace();
        }
    }
}