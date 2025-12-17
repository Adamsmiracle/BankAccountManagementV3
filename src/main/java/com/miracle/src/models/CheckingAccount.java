package com.miracle.src.models;

import com.miracle.src.models.exceptions.InsufficientFundsException;
import com.miracle.src.models.exceptions.InvalidAmountException;
import com.miracle.src.models.exceptions.OverdraftExceededException;
import com.miracle.src.services.TransactionManager;

public class CheckingAccount extends Account {

    private double overDraftLimit = 1000.00; // Updated overdraft limit to $1000.00
    private final double monthlyFee = 10.00;
    private final TransactionManager manager = TransactionManager.getInstance();

    public CheckingAccount(Customer customer, double initialDeposit) throws InvalidAmountException {
        super(customer);
        this.setStatus("Active");
        if (initialDeposit <= 0){
            throw new InvalidAmountException(initialDeposit);
        }

        super.updateBalance(initialDeposit);
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
    public synchronized Transaction deposit(double amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException(amount);
        }
        Transaction newTransaction;
            super.updateBalance(this.getBalance() + amount);
            newTransaction = new Transaction(
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
    public synchronized Transaction withdraw(double amount) throws InvalidAmountException, OverdraftExceededException, InsufficientFundsException {
        if (amount <= 0) {
            throw new InvalidAmountException(amount);
        }

        double available = super.getBalance() + overDraftLimit;
        if (amount > available) {
            throw new InsufficientFundsException(
                    String.format("Insufficient funds including overdraft. Available: $%,.2f, Attempted: $%,.2f",
                            available, amount));
        }

        Transaction newTransaction;

            double resultingBalance = this.getBalance() - amount;

            if (amount > this.getBalance() + overDraftLimit) {
                throw new OverdraftExceededException(this.getBalance(), amount, overDraftLimit);
            }

            if (resultingBalance < -overDraftLimit) {
                throw new OverdraftExceededException(this.getBalance(), amount, overDraftLimit);
            }

            super.updateBalance(resultingBalance);
            newTransaction = new Transaction(
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
                super.updateBalance(super.getBalance() - monthlyFee);
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

    @Override
    public synchronized Transaction depositWithType(double amount, String transactionType) {
        if (amount <= 0) {
            System.out.println("Deposit amount must be positive.");
            return null;
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
    public synchronized Transaction withdrawWithType(double amount, String transactionType) throws InvalidAmountException, OverdraftExceededException {
        if (amount <= 0) {
            throw new InvalidAmountException(amount);
        }
        Transaction newTransaction;
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

            super.updateBalance(resultingBalance);

            newTransaction = new Transaction(
                    this.getAccountNumber(),
                    transactionType,
                    amount,
                    resultingBalance
            );
        manager.addTransaction(newTransaction);
        return newTransaction;
    }




}