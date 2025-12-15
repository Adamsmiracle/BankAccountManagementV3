package com.miracle.src.services;

import com.miracle.src.dto.AccountRequest;
import com.miracle.src.dto.TransactionRequest;
import com.miracle.src.models.*;
import com.miracle.src.models.exceptions.AccountNotFoundException;
import com.miracle.src.models.exceptions.InvalidAmountException;
import com.miracle.src.models.exceptions.OverdraftExceededException;
import com.miracle.src.models.exceptions.TransactionFailedException;

import java.util.ArrayList;
import java.util.List;

public class AccountManager {

    // Singleton instance
    private static final AccountManager INSTANCE = new AccountManager();

    public static AccountManager getInstance() {
        return INSTANCE;
    }
    private final List<Account> accounts = new ArrayList<>();
    private AccountManager() {
    }

    private int accountCount = 0;


    public void addAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        
        // Ensure account is not already added
        if (accounts.contains(account)) {
            throw new IllegalArgumentException("Account already exists in the system");
        }
        
        accounts.add(account);
        accountCount++;
    }


    //    linear search through the Accounts array to find an account using
//    the account number
    public Account findAccount(String accountNumber) throws AccountNotFoundException {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new AccountNotFoundException("Account number cannot be empty");
        }

        return accounts.stream()
                .filter(acc -> acc.getAccountNumber().equalsIgnoreCase(accountNumber))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found. Please check the account number and try again.",
                        accountNumber
                ));
    }

    //    Get all opened accounts in the banks
    public void viewAllAccounts() {
        System.out.println("\n ACCOUNT LISTING ");
        System.out.println("-".repeat(83));
        System.out.printf("| %-8s | %-25s | %-12s | %-14s | %-8s |%n",
                "ACC NO", "CUSTOMER NAME", "TYPE", "BALANCE", "STATUS");
        System.out.println("-".repeat(83));

        for (Account account : accounts) {
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
        if(accountCount == 0) {
            System.out.println("No accounts found." );
            return;
        }
        System.out.printf("%nTotal Accounts: %d %n", getAccountCount());
        System.out.printf("Total Bank Balance: $%,.2f%n", getTotalBalance());
    }


    //    Get all the money available at the bank.
    public double getTotalBalance() {
        return accounts.stream()
                .mapToDouble(Account::getBalance)
                .sum();
    }

    //    Get the number of accounts opened at the bank.
    public int getAccountCount() {
        return accountCount;
    }


    public void createAccount(AccountRequest req) throws InvalidAmountException {

//        Creating the customer based on the account type selected
        Customer customer = null;
        if (req.getCustomerType() == 1) {
            customer = new RegularCustomer(req.getName(), req.getAge(), req.getContact(), req.getAddress());
        } else {
            customer = new PremiumCustomer(req.getName(), req.getAge(), req.getContact(), req.getAddress());
        }

//        Creating the account based on the type selected
        Account account;
        if (req.getAccountType() == 1) {
            account = new SavingsAccount(customer, req.getInitialDeposit());
        } else {
            account = new CheckingAccount(customer, req.getInitialDeposit());
        }
        account.displayAccountDetails();
        addAccount(account);
    }


    public void processTransaction(TransactionRequest request)
            throws InvalidAmountException, OverdraftExceededException, AccountNotFoundException, TransactionFailedException {
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

                // Process transfer as an atomic operation
                try {
                    // Start transaction context
                    TransactionManager txManager = TransactionManager.getInstance();
                    txManager.beginTransaction(userAccount, receiverAccount);

                    // Process withdrawal from source
                    userAccount.processTransaction(amount, "Transfer");

                    // Process deposit to target
                    receiverAccount.processTransaction(amount, "Receive");

                    // Commit transaction if everything succeeds
                    txManager.commit();

                } catch (Exception e) {
                    // Rollback will be handled by the TransactionManager
                    TransactionManager.getInstance().rollback(); // Explicit rollback in case of failure
                    throw e;
                }
                break;

            default:
                throw new IllegalArgumentException("Invalid transaction type: " + transactionType);
        }
    }



    public void displayAllCustomers() {
        if (accountCount == 0) {
            System.out.println("\nNo customers found.");
            return;
        }

        System.out.printf("%nALL CUSTOMERS%n");
        System.out.println("=".repeat(100));
        System.out.printf("| %-6s | %-20s | %-5s | %-15s | %-20s | %-15s|%n", "ID", "NAME", "AGE", "CONTACT", "ADDRESS", "ACCOUNT TYPE");
        System.out.println("-".repeat(100));

        for (Account account: accounts) {
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
        System.out.printf("Total Customers: %d%n", accountCount);
    }


    // Add this method to get transaction history for an account
    public void displayTransactionHistory(String accountNumber) throws AccountNotFoundException {
        Account account = findAccount(accountNumber);
        System.out.println("\nTransaction History for Account: " + accountNumber);
        System.out.println("=".repeat(100));

        // Get transactions from TransactionManager
        List<Transaction> transactions = TransactionManager.getInstance()
                .getTransactionsByAccount(accountNumber);

        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        // Display header
        System.out.printf("| %-10s | %-20s | %-15s | %-12s | %-12s |%n",
                "ID", "Date/Time", "Type", "Amount", "Balance");
        System.out.println("-".repeat(80));

        // Display transactions
        for (Transaction t : transactions) {
            t.displayTransactionDetails();
        }

        System.out.println("=".repeat(80));
        System.out.printf("Current Balance: $%,.2f%n", account.getBalance());
    }
}
