package com.miracle.src.dto;

public class TransactionRequest {
    private String userAccountNumber;
    private String receiverAccountNumber;
    private String transactionType;
    private double amount;

    public TransactionRequest(String userAccountNumber, String receiverAccountNumber, String transactionType, double amount) {
        this.userAccountNumber = userAccountNumber;
        this.receiverAccountNumber = receiverAccountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
    }

    public String getUserAccountNumber() {
        return userAccountNumber;
    }

    public void setUserAccountNumber(String userAccountNumber) {
        this.userAccountNumber = userAccountNumber;
    }

    public String getReceiverAccountNumber() {
        return receiverAccountNumber;
    }

    public void setReceiverAccountNumber(String receiverAccountNumber) {
        this.receiverAccountNumber = receiverAccountNumber;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
