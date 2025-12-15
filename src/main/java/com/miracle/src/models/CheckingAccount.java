package com.miracle.src.models;

import com.miracle.src.models.exceptions.InvalidAmountException;
import com.miracle.src.models.exceptions.OverdraftExceededException;
import com.miracle.src.services.TransactionManager;

public class CheckingAccount extends Account {

    private double overDraftLimit = 1000.00; // Updated overdraft limit to $1000.00
    private final double monthlyFee = 10.00;
    private final double minimumBalance = 50.00; // Minimum balance requirement
    private double initialDeposit;
    private final TransactionManager manager = TransactionManager.getInstance();

    public CheckingAccount(Customer customer, double initialDeposit) throws InvalidAmountException {
        super(customer);
        this.setStatus("Active");
        this.initialDeposit = initialDeposit;
        if (initialDeposit <= 0){
            throw new InvalidAmountException(initialDeposit);
        }

        super.setBalance(initialDeposit);
        // Log initial deposit as a transaction
        Transaction initialTransaction = new Transaction(
            this.getAccountNumber(),
            "Deposit",
            initialDeposit,
            initialDeposit
        );
        manager.addTransaction(initialTransaction);
    }

 

    @Override
    protected void displaySpecificDetails() {
        System.out.printf("Overdraft Limit: $%,.2f\n", getOverDraftLimit());
        
        if (getCustomer() instanceof PremiumCustomer && ((PremiumCustomer) getCustomer()).hasWaivedFees()) {
            System.out.println("Monthly Fee: Waived (Premium customer)");
        } else {
            System.out.printf("Monthly Fee: $%,.2f\n", getMonthlyFee());
        }
    }

    @Override
    public Transaction deposit(double amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException(amount);
        }

        this.setBalance(this.getBalance() + amount);
        Transaction newTransaction = new Transaction(
                this.getAccountNumber(),
                "Deposit",
                amount,
                this.getBalance()
        );
        manager.addTransaction(newTransaction);
        return newTransaction;
    }

    @Override
    public String getAccountType() {
        return "Checking";
    }



    @Override
    public Transaction withdraw(double amount) throws InvalidAmountException, OverdraftExceededException {
        if (amount <= 0) {
            throw new InvalidAmountException(amount);
        }

        double resultingBalance = this.getBalance() - amount;

        if (amount > this.getBalance() + overDraftLimit) {
            throw new OverdraftExceededException(this.getBalance(), amount, overDraftLimit);
        }

        // if (resultingBalance < minimumBalance) {
        //     throw new OverdraftExceededException(this.getBalance(), amount, minimumBalance);
        // }

        if (resultingBalance < -overDraftLimit) {
            throw new OverdraftExceededException(this.getBalance(), amount, overDraftLimit);
        }

        this.setBalance(resultingBalance);
        Transaction newTransaction = new Transaction(
                this.getAccountNumber(),
                "Withdrawal",
                -amount,
                this.getBalance()
        );
        manager.addTransaction(newTransaction);
        return newTransaction;
    }



    public boolean applyMonthlyFee() {
        Customer c = getCustomer();
        if (c instanceof PremiumCustomer && ((PremiumCustomer) c).hasWaivedFees()) {
            System.out.println("Monthly fee waived for Premium customer.");
            return true;
        }

        if (super.getBalance() - monthlyFee >= -overDraftLimit) {
            super.setBalance(super.getBalance() - monthlyFee);
            return true;
        }
        return false;
    }

    public double getMonthlyFee() {
        return monthlyFee;
    }

    public double getOverDraftLimit() {
        return overDraftLimit;
    }

    public double getMinimumBalance() {
        return minimumBalance;
    }



    @Override
    public Transaction depositWithType(double amount, String transactionType) {
        if (amount <= 0) {
            System.out.println("Deposit amount must be positive.");
            return null;
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
    public Transaction withdrawWithType(double amount, String transactionType) throws InvalidAmountException, OverdraftExceededException {
        if (amount <= 0) {
            throw new InvalidAmountException(amount);
        }
        double resultingBalance = this.getBalance() - amount;

        if (resultingBalance < -getOverDraftLimit()) {
            throw new OverdraftExceededException(
                this.getBalance(),
                amount,
                getOverDraftLimit()
            );
        }

        if (amount > this.getBalance() + overDraftLimit) {
            throw new OverdraftExceededException(this.getBalance(), amount, overDraftLimit);
        }

        // Ensure overdraft limit is dynamically adjustable
        if (getCustomer() instanceof PremiumCustomer) {
            this.overDraftLimit = 200.00; // Example adjustment for premium customers
        }

        super.setBalance(resultingBalance);

        Transaction newTransaction = new Transaction(
                this.getAccountNumber(),
                transactionType,
                amount,
                resultingBalance
        );
        manager.addTransaction(newTransaction);
        return newTransaction;
    }




}