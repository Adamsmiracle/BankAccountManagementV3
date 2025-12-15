package com.miracle.src.models.exceptions;

public class AccountNotFoundException extends Exception {

    public AccountNotFoundException(String accountNumber) {

        super("Account not found: " + accountNumber);
    }

    public AccountNotFoundException(String message, String accountNumber) {
        super(message + ": " + accountNumber);
    }

}
