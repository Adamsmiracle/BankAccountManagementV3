package com.miracle.src.models;

import com.miracle.src.services.AccountManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Transaction {

    // Static fields
    private static int transactionCounter;

    // Private fields
    private final String transactionId;
    private String accountNumber;
    private String type;
    private double amount;
    private double balanceAfter;
    private final LocalDateTime timestamp;
    public static final DateTimeFormatter TIMESTAMP_FORMATTER =
        DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a");

    public Transaction(String accountNumber, String type, double amount, double balanceAfter) {
        transactionCounter++;
        this.transactionId = String.format("TXN%03d", transactionCounter);
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = LocalDateTime.now();
    }

    public Transaction(String transactionId, String accountNumber, String type,
                       double amount, double balanceAfter, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = timestamp;
        // Ensure the counter is at least as high as this ID number
        updateCounterFromId(transactionId);
    }


    private static void updateCounterFromId(String id) {
        try {
            Pattern p = Pattern.compile("TXN(\\d{3,})");
            Matcher m = p.matcher(id);
            if (m.matches()) {
                int num = Integer.parseInt(m.group(1));
                if (num > transactionCounter) {
                    transactionCounter = num;
                }
            }
        } catch (Exception ignored) {
        }
    }

    public String getFormattedTimestamp() {
        return this.timestamp.format(TIMESTAMP_FORMATTER);
    }

    // GETTERS AND SETTERS
    public static int getTransactionCounter() {
        return transactionCounter;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }


    public LocalDateTime getTimestamp() {
        return timestamp;
    }

}