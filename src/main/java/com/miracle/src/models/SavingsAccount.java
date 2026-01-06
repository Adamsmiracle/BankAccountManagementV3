package com.miracle.src.models;

import com.miracle.src.models.exceptions.InsufficientFundsException;
import com.miracle.src.models.exceptions.InvalidAmountException;
import com.miracle.src.services.TransactionManager;

import java.io.Serializable;

public class SavingsAccount extends Account implements Serializable {

    // --- Private Fields
    private final double interestRate = 0.035;
    private final static double minimumBalance = 500.00;
    private final TransactionManager manager = TransactionManager.getInstance();

    public SavingsAccount(Customer customer, double initialDeposit) throws InvalidAmountException {
        // 1. VALIDATE EVERYTHING FIRST (before changing ANY state)
        if (initialDeposit <= 0) {
            throw new InvalidAmountException(initialDeposit);
        }

        if (initialDeposit < minimumBalance) {
            throw new InvalidAmountException(
                    "Initial deposit must be at least $" + minimumBalance,
                    initialDeposit
            );
        }
        super(customer);  // Creates account
        this.setStatus("Active");
        super.updateBalance(initialDeposit);

        try {
            Transaction initialTransaction = new Transaction(
                    this.getAccountNumber(),
                    "Deposit",
                    initialDeposit,
                    initialDeposit
            );
            manager.addTransaction(initialTransaction);
        } catch (Exception e) {
            System.out.println("Account Creation failed");
        }
    }

    /**
     * Constructor for loading account from file.
     * Does NOT create an initial deposit transaction and preserves the account number.
     *
     * @param customer the customer
     * @param balance the current balance
     * @param accountNumber the original account number to preserve
     * @param fromFile flag to indicate this is loaded from file (just for method signature distinction)
     */
    public SavingsAccount(Customer customer, double balance, String accountNumber, boolean fromFile) {
        super(customer, accountNumber, fromFile);  // Use the file-loading constructor
        this.setStatus("Active");
        super.updateBalance(balance);
        // No transaction is created - it should already exist in transactions.txt
    }



    @Override
    public String getAccountType() {
        return "Savings";
    }

    @Override
    protected void displaySpecificDetails() {
        System.out.printf("Interest Rate: %.1f%%\n", getInterestRate() * 100);
        System.out.printf("Minimum Balance: $%,.2f\n", getMinimumBalance());
    }

    @Override
    public synchronized Transaction deposit(double amount) throws InvalidAmountException {
        return depositWithType(amount, "Deposit");
    }

    @Override
    public synchronized Transaction depositWithType(double amount, String transactionType) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException(amount);
        }

        if (transactionType == null || transactionType.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction type cannot be null or empty");
        }

        Transaction newTransaction;

            super.updateBalance(this.getBalance() + amount);

            newTransaction = new Transaction(
                    this.getAccountNumber(),
                    transactionType,
                    amount,
                    this.getBalance()
            );
        manager.addTransaction(newTransaction);
        return newTransaction;
    }

    @Override
    public synchronized Transaction withdraw(double amount) throws InvalidAmountException, InsufficientFundsException {
        if (amount <= 0){
            throw new InvalidAmountException(amount);
        }

        double minBalance = getMinimumBalance();
        if (super.getBalance() - amount < minBalance) {
            throw new InsufficientFundsException(
                    String.format("Withdrawal would violate minimum balance requirement of $%,.2f", minBalance));
        }

        return withdrawWithType(amount, "Withdrawal");
    }

    @Override
    public synchronized Transaction withdrawWithType(double amount, String transactionType)
            throws InvalidAmountException {

        if (amount <= 0) {
            throw new InvalidAmountException(amount);
        }

        // Validate transaction type
        if (transactionType == null || transactionType.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction type cannot be null or empty");
        }

        Transaction newTransaction;
            // Calculate resulting balance with current snapshot
            double resultingBalance = this.getBalance() - amount;

            // Check against minimum balance
            if (resultingBalance < minimumBalance) {
                java.util.Scanner scanner = new java.util.Scanner(System.in);
                while (true) {
                    System.out.printf(
                        "Transaction failed: Withdrawal of $%.2f would result in a balance of $%.2f, which is below the minimum balance of $%.2f.\n",
                        amount, resultingBalance, minimumBalance
                    );
                    System.out.println("Please enter a new amount that will not violate the minimum balance, or type 0 to go back:");
                    double newAmount = scanner.nextDouble();

                    if (newAmount == 0) {
                        return null; // Exit the transaction
                    }

                    resultingBalance = this.getBalance() - newAmount;
                    if (resultingBalance >= minimumBalance) {
                        amount = newAmount; // Update the amount to the valid value
                        break;
                    }
                }
            }

            // Update account balance
            double resultingBalanceFinal = this.getBalance() - amount;
            super.updateBalance(resultingBalanceFinal);

            // Record the transaction (amount is always positive, type indicates direction)
            newTransaction = new Transaction(
                    this.getAccountNumber(),
                    transactionType,
                    amount,
                    this.getBalance()
            );
        manager.addTransaction(newTransaction);
        return newTransaction;
    }



    public double calculateInterest() {
        return this.getInterestRate() * super.getBalance();
    }

    public double getInterestRate() {
        return interestRate;
    }

    public static double getMinimumBalance() {
        return minimumBalance;
    }
}