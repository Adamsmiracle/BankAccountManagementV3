package com.miracle.src.models.exceptions;

// Extend the base Exception class instead of RuntimeException
public class InsufficientFundsException extends Exception {
    private final double currentBalance;
    private final double requiredAmount;

    public InsufficientFundsException(double currentBalance, double requiredAmount) {
        super(String.format("Insufficient funds. Current balance: $%.2f, Required: $%.2f", currentBalance, requiredAmount));
        this.currentBalance = currentBalance;
        this.requiredAmount = requiredAmount;
    }

    public InsufficientFundsException(String customMessage) {
        super(customMessage);
        this.currentBalance = 0.0; // Default value
        this.requiredAmount = 0.0; // Default value
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public double getRequiredAmount() {
        return requiredAmount;
    }
}


