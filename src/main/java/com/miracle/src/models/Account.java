package com.miracle.src.models;

import com.miracle.src.models.exceptions.InvalidAmountException;
import com.miracle.src.models.exceptions.OverdraftExceededException;

import java.io.Serializable;

public abstract class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    public static int accountCounter = 0;

    //    private field
    private String accountNumber;
    // Balance is accessed by multiple threads during concurrent transactions.
    // Use volatile + per-account lock to ensure visibility and atomic updates.
    private volatile double balance;
    private String status = "Active";
    private final Customer customer;
    // Per-account lock to synchronize critical sections (deposits/withdrawals)
    private final Object balanceLock = new Object();


    public Account(Customer customer) {
        accountCounter++;
        this.customer = customer;
        this.accountNumber = String.format("ACC%03d", accountCounter);
    }

    // Abstract methods for deposit and withdraw
    public abstract Transaction deposit(double amount) throws InvalidAmountException;
    public abstract Transaction withdraw(double amount) throws InvalidAmountException, OverdraftExceededException;
    
    // Overloaded methods with transaction type for transfers
    public abstract Transaction depositWithType(double amount, String transactionType)
            throws InvalidAmountException;
    public abstract Transaction withdrawWithType(double amount, String transactionType)
            throws InvalidAmountException, OverdraftExceededException;

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

    public void setBalance(double balance) {
        this.balance = balance;
    }

    /**
     * Thread-safe balance update helper. Sets the balance inside the per-account lock
     * and returns the updated value. Subclasses should prefer this over direct setBalance
     * when changing the balance.
     */
    public synchronized double updateBalance(double newBalance) {
        synchronized (balanceLock) {
            this.balance = newBalance;
            return this.balance;
        }
    }

    /**
     * Expose the per-account lock to subclasses for synchronizing critical updates.
     */
    protected Object getBalanceLock() {
        return balanceLock;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // ABSTRACT METHODS
    public abstract String getAccountType();
    
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

    }


}