package com.miracle.src.models;

import com.miracle.src.models.exceptions.InvalidAmountException;
import com.miracle.src.models.exceptions.OverdraftExceededException;

public abstract class Account {
    public static int accountCounter = 0;

    //    private field
    private String accountNumber;
    private double balance;
    private String status = "Active";
    private final Customer customer;


    public Account(Customer customer) {
        accountCounter++;
        this.customer = customer;
        this.accountNumber = String.format("ACC%03d", accountCounter);
    }

    // Abstract methods for deposit and withdraw
    public abstract Transaction deposit(double amount) throws InvalidAmountException;
    public abstract Transaction withdraw(double amount) throws InvalidAmountException, OverdraftExceededException;
    
    // Overloaded methods with transaction type for transfers
    public abstract Transaction depositWithType(double amount, String transactionType) throws InvalidAmountException;
    public abstract Transaction withdrawWithType(double amount, String transactionType) throws InvalidAmountException, OverdraftExceededException;

    // GETTERS
    public String getAccountNumber() {
        return accountNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public double getBalance() {
        return balance;
    }

    public String getStatus() {
        return status;
    }

    // SETTERS
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // ABSTRACT METHODS
    public abstract String getAccountType();
    
    // Template method for displaying account details
    public void displayAccountDetails() {
        System.out.println("✔ Account created successfully!");
        System.out.println("Account Number: " + getAccountNumber());
        System.out.println("Customer: " + getCustomer().getCustomerId() + " - " + 
                          getCustomer().getName() + " (" + getCustomer().getCustomerType() + ")");
        System.out.println("Account Type: " + getAccountType());
        System.out.printf("Initial Balance: $%,.2f\n", getBalance());
        
        // Call abstract method for account-specific details
        displaySpecificDetails();
        
        System.out.println("Status: " + getStatus());
        System.out.println("\n");
    }
    
    // Abstract method for subclass-specific details
    protected abstract void displaySpecificDetails();


    public void processTransaction(double amount, String type) throws InvalidAmountException, OverdraftExceededException {
        if (amount <= 0) {
            throw new InvalidAmountException(amount);
        }

        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction type cannot be null or empty");
        }

        Transaction result;

        if (type.equalsIgnoreCase("Deposit")) {
            result = this.deposit(amount);
        } else if (type.equalsIgnoreCase("Withdrawal")) {
            result = this.withdraw(amount);
        } else if (type.equalsIgnoreCase("Transfer")) {
            result = this.withdrawWithType(amount, "Transfer Out");
        } else if (type.equalsIgnoreCase("Receive")) {
            result = this.depositWithType(amount, "Transfer In");
        } else {
            throw new IllegalArgumentException("Invalid transaction type: " + type);
        }

        if (result == null) {
            return; // Exit gracefully without logging success for failed transactions
        }
    }
}