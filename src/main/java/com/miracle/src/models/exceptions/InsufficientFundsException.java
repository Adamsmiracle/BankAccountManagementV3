package com.miracle.src.models.exceptions;

public class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(double balance, double amount) {
        super(String.format("Insufficient funds. Current balance: $%,.2f, Required: $%,.2f",
                balance, amount));
    }
}