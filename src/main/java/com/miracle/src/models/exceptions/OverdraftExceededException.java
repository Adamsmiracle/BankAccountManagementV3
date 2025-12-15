// src/models/exceptions/OverdraftExceededException.java
package com.miracle.src.models.exceptions;

public class OverdraftExceededException extends Exception {
    private double balance;
    private double withdrawalAmount;
    private double overdraftLimit;

    public OverdraftExceededException(double balance, double withdrawalAmount, double overdraftLimit) {
        super(String.format(
                "Overdraft limit exceeded. Balance: $%.2f, Withdrawal: $%.2f, " +
                        "Resulting balance: $%.2f would exceed overdraft limit of $%.2f",
                balance, withdrawalAmount, (balance - withdrawalAmount), overdraftLimit
        ));
        this.balance = balance;
        this.withdrawalAmount = withdrawalAmount;
        this.overdraftLimit = overdraftLimit;
    }

    public double getBalance() {
        return balance;
    }

    public double getWithdrawalAmount() {
        return withdrawalAmount;
    }

    public double getOverdraftLimit() {
        return overdraftLimit;
    }
}