package com.miracle.src.models;

import com.miracle.src.models.exceptions.InvalidAmountException;
import com.miracle.src.services.TransactionManager;

public class SavingsAccount extends Account {

    // --- Private Fields
    private final double interestRate;
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
        this.interestRate = 0.035;
        this.setStatus("Active");
        super.setBalance(initialDeposit);

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
    public Transaction deposit(double amount) throws InvalidAmountException {
        return depositWithType(amount, "Deposit");
    }

    @Override
    public Transaction depositWithType(double amount, String transactionType) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException(amount);
        }

        if (transactionType == null || transactionType.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction type cannot be null or empty");
        }

        this.setBalance(this.getBalance() + amount);

        Transaction newTransaction = new Transaction(
                this.getAccountNumber(),
                transactionType,
                amount,
                this.getBalance()
        );

        manager.addTransaction(newTransaction);
        return newTransaction;
    }

    @Override
    public Transaction withdraw(double amount) throws InvalidAmountException {
        if (amount <= 0){
            throw new InvalidAmountException(amount);
        }
        return withdrawWithType(amount, "Withdrawal");
    }

    @Override
    public Transaction withdrawWithType(double amount, String transactionType)
            throws InvalidAmountException {

        if (amount <= 0) {
            throw new InvalidAmountException(amount);
        }

        // Validate transaction type
        if (transactionType == null || transactionType.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction type cannot be null or empty");
        }

        // Calculate resulting balance
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
        super.setBalance(resultingBalance);

        // Record the transaction
        Transaction newTransaction = new Transaction(
                this.getAccountNumber(),
                transactionType,
                amount,
                resultingBalance
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