// src/models/exceptions/InvalidAmountException.java
package com.miracle.src.models.exceptions;

public class InvalidAmountException extends Exception {
    private double amount;

    public InvalidAmountException(double amount) {
        super("Invalid amount: $" + amount + ". Amount must be positive.");
        this.amount = amount;
    }

    public InvalidAmountException(String message, double amount) {
        super(message);
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }
}