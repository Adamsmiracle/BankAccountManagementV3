package com.miracle.src.services;

import com.miracle.src.dto.AccountRequest;
import com.miracle.src.dto.TransactionRequest;
import com.miracle.src.models.*;
import com.miracle.src.models.exceptions.AccountNotFoundException;
import com.miracle.src.models.exceptions.InvalidAmountException;
import com.miracle.src.models.exceptions.OverdraftExceededException;
import com.miracle.src.utils.FileIOUtils;
//import com.miracle.src.utils.FileIOUtils;

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
        List<Account> sortedAccounts = new ArrayList<>(this.accounts.values());
        sortedAccounts.sort(Comparator.comparing(Account::getAccountNumber));
        for (Account account : sortedAccounts) {
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
        }

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
        // Track newly created accounts
        newlyCreatedAccountNumbers.add(account.getAccountNumber());
    }

    public void createAccountFromFile(AccountRequest req) throws InvalidAmountException {
        Account account = createAccountInternal(req);
        addAccount(account);
    }


    public Collection<Account> getAllAccounts() {
        return accounts.values();
    }

    public void processTransaction(TransactionRequest request)
            throws InvalidAmountException, OverdraftExceededException, AccountNotFoundException {
        if (request == null) {
            throw new IllegalArgumentException("Transaction request cannot be null");
        }

        String transactionType = request.getTransactionType();
        String userAccountNumber = request.getUserAccountNumber();
        double amount = request.getAmount();

        // Find the source account
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
                userAccount.processTransaction(amount, "Transfer");
                receiverAccount.processTransaction(amount, "Receive");
                break;

            default:
                throw new IllegalArgumentException("Invalid transaction type: " + transactionType);
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

            FileIOUtils.appendAccountsToFile(newAccounts);

            // Clear the tracker after persisting
            newlyCreatedAccountNumbers.clear();
        } catch (Exception e) {
            System.err.println("Critical error saving accounts: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
