package com.miracle.src.services;

import com.miracle.src.dto.AccountRequest;
import com.miracle.src.dto.TransactionRequest;
import com.miracle.src.models.*;
import com.miracle.src.models.exceptions.AccountNotFoundException;
import com.miracle.src.models.exceptions.InsufficientFundsException;
import com.miracle.src.models.exceptions.InvalidAmountException;
import com.miracle.src.models.exceptions.OverdraftExceededException;
import com.miracle.src.utils.FileIOUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AccountManager {

    // Singleton instance
    private AtomicInteger accountCount =  new AtomicInteger(0);
    private static final AccountManager INSTANCE = new AccountManager();

    public static AccountManager getInstance() {
        return INSTANCE;
    }

//    map for storing accounts
    private Map<String, Account> accounts = new HashMap<>();

//    set for tracking newly created accounts
    private Set<String> newlyCreatedAccountNumbers = new HashSet<>();

    private AccountManager() {
    }



    public void setAccountCount(AtomicInteger accountCount) {
        this.accountCount = accountCount;
    }

    public boolean addAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        if (accounts.containsKey(account.getAccountNumber())) {
            throw new IllegalArgumentException("Account already exists in the system");
        }

        accounts.put(account.getAccountNumber(), account);
        accountCount.getAndIncrement();
        // Track as newly created so it gets saved to file
        newlyCreatedAccountNumbers.add(account.getAccountNumber());
        return true;
    }

    /**
     * Adds an account that was loaded from file.
     * Does NOT track it as newly created (won't be re-saved).
     */
    public boolean addAccountFromFile(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        if (accounts.containsKey(account.getAccountNumber())) {
            // Skip duplicate accounts silently during file load
            return false;
        }

        accounts.put(account.getAccountNumber(), account);
        accountCount.getAndIncrement();
        // Don't add to newlyCreatedAccountNumbers - this was loaded from file
        return true;
    }



    // Get the account using the account number (key)
    public Account findAccount(String accountNumber) throws AccountNotFoundException {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new AccountNotFoundException("Account number cannot be empty");
        }
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
        return account;
    }

    //    Get all opened accounts in the banks
    public void viewAllAccounts() {
        System.out.println("\n ACCOUNT LISTING ");
        System.out.println("-".repeat(83));
        System.out.printf("| %-8s | %-25s | %-12s | %-14s | %-8s |%n",
                "ACC NO", "CUSTOMER NAME", "TYPE", "BALANCE", "STATUS");
        System.out.println("-".repeat(83));

        // Sort accounts by account number in descending order using streams
        accounts.values().stream()
                .sorted((a1, a2) -> a2.getAccountNumber().compareTo(a1.getAccountNumber()))
                .forEach(account -> {
                    // Line 1: Main Account Details
                    System.out.printf("| %-8s | %-25s | %-12s | $%,-13.2f | %-8s |%n",
                            account.getAccountNumber(),
                            account.getCustomer().getName(),
                            account.getAccountType(),
                            account.getBalance(),
                            account.getStatus()
                    );

                    // Line two of the output formatter
                    if (account instanceof SavingsAccount savingsAccount) {
                        System.out.printf("| %-8s | Interest Rate: %.1f%% | Min Balance: $%,.2f |%n",
                                "",
                                savingsAccount.getInterestRate() * 100,
                                SavingsAccount.getMinimumBalance()
                        );
                    } else if (account instanceof CheckingAccount checkingAccount) {
                        System.out.printf("| %-8s | Overdraft Limit: $%,.2f | Monthly Fee: $%,.2f |%n",
                                "",
                                checkingAccount.getOverDraftLimit(),
                                account.getCustomer().getCustomerType().equals("Premium") ? 0.00 : checkingAccount.getMonthlyFee()
                        );
                    }
                    System.out.println("-".repeat(83));
                });

        // Display required totals
        if(accountCount.get() == 0) {
            System.out.println("No accounts found." );
        } else{
             System.out.printf("%nTotal Accounts: %d%n", getAccountCount());
             System.out.printf("Total Bank Balance: $%,.2f%n", getTotalBalance());
        }
       
    }


    public double getTotalBalance() {
        return accounts.values().stream()
                .parallel()
                .mapToDouble(Account::getBalance)
                .sum();
    }

    public static void loadAccountsOnStart() throws IOException {
        FileIOUtils.readAccountsFromFile();
    }

    //    Get the number of accounts opened at the bank.
    public int getAccountCount() {
        return accountCount.get();
    }


    private Account createAccountInternal(AccountRequest req) throws InvalidAmountException {
        // Input validation
        if (req == null) {
            throw new IllegalArgumentException("Account request cannot be null");
        }

        Customer customer = (req.getCustomerType() == 1)
                ? new RegularCustomer(req.getName(), req.getAge(), req.getContact(), req.getAddress())
                : new PremiumCustomer(req.getName(), req.getAge(), req.getContact(), req.getAddress());

        return (req.getAccountType() == 1)
                ? new SavingsAccount(customer, req.getInitialDeposit())
                : new CheckingAccount(customer, req.getInitialDeposit());
    }

    // 3. Simplified createAccount methods
    public void createAccount(AccountRequest req) throws InvalidAmountException {
        Account account = createAccountInternal(req);
        account.displayAccountDetails();
        addAccount(account);
        // addAccount() now tracks newly created accounts automatically
    }



    public Collection<Account> getAllAccounts() {
        return accounts.values();
    }


    // In AccountManager.java
    public void processTransaction(TransactionRequest request)
            throws InvalidAmountException, AccountNotFoundException, InsufficientFundsException, OverdraftExceededException {
        if (request == null) {
            throw new IllegalArgumentException("Transaction request cannot be null");
        }

        String transactionType = request.getTransactionType();
        String userAccountNumber = request.getUserAccountNumber();
        double amount = request.getAmount();

        try {
            Account userAccount = findAccount(userAccountNumber);

            switch (transactionType.toUpperCase()) {
                case "DEPOSIT":
                    userAccount.processTransaction(amount, "Deposit");
                    break;
                case "WITHDRAWAL":
                    userAccount.processTransaction(amount, "Withdrawal");
                    break;
                case "TRANSFER":
                    String receiverAccountNumber = request.getReceiverAccountNumber();
                    if (receiverAccountNumber == null || receiverAccountNumber.trim().isEmpty()) {
                        throw new IllegalArgumentException("Receiver account number is required for transfer");
                    }


                    Account receiverAccount = findAccount(receiverAccountNumber);
                    // First withdraw from source
                    userAccount.processTransaction(amount, "Transfer");
                    // Then deposit to receiver
                    try {
                        receiverAccount.processTransaction(amount, "Receive");
                    } catch (Exception e) {
                        // If deposit to receiver fails, refund the source account
                        userAccount.deposit(amount);
                        throw new InsufficientFundsException("Transfer failed: " + e.getMessage());
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Invalid transaction type: " + transactionType);
            }
        } catch (AccountNotFoundException e) {
            System.err.println("Account not found: " + e.getMessage());
            throw e;
        } catch (InsufficientFundsException e) {
            System.err.println("Insufficient funds for transaction: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Transaction failed: " + e.getMessage());
            throw new RuntimeException("Transaction processing failed", e);
        }
    }

    public void displayAllCustomers() {
        if (accountCount.get() == 0) {
            System.out.println("\nNo customers found.");
            return;
        }

        System.out.printf("%nALL CUSTOMERS%n");
        System.out.println("=".repeat(100));
        System.out.printf("| %-6s | %-20s | %-5s | %-15s | %-20s | %-15s|%n", "ID", "NAME", "AGE", "CONTACT", "ADDRESS", "ACCOUNT TYPE");
        System.out.println("-".repeat(100));

        for (Account account: accounts.values()) {
            Customer c = account.getCustomer();

            System.out.printf("| %-6s | %-20s | %-5d | %-15s | %-20s | %-15s|%n",
                    c.getCustomerId(),   // if you have a customer ID
                    c.getName(),
                    c.getAge(),
                    c.getContact(),
                    c.getAddress(),
                    account.getAccountType()
            );
        }

        System.out.println("=".repeat(100));
        System.out.println("Total Customers: "+ accountCount);
    }



    public void saveAccountsOnExit() {
        try {
            Map<String, Account> newAccounts = new HashMap<>();
            for (String accNo : newlyCreatedAccountNumbers) {
                Account acc = accounts.get(accNo);
                if (acc != null) {
                    newAccounts.put(accNo, acc);
                }
            }

            if (newAccounts.isEmpty()) {
                System.out.println("No new accounts to save.");
                return;
            }

            FileIOUtils.saveAccountsToFile(newAccounts);

            // Clear the tracker after persisting
            newlyCreatedAccountNumbers.clear();
        } catch (Exception e) {
            System.err.println("Critical error saving accounts: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
