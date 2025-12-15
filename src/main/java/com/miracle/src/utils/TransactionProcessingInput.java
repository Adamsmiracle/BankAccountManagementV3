package com.miracle.src.utils;

import com.miracle.src.dto.TransactionRequest;
import com.miracle.src.models.Account;
import com.miracle.src.models.SavingsAccount;
import com.miracle.src.services.AccountManager;

public class TransactionProcessingInput {

    private static final AccountManager accountManager = AccountManager.getInstance();

    private TransactionProcessingInput() {}

    public static TransactionRequest processTransactionMain() throws InterruptedException {
        System.out.println("\nPROCESS TRANSACTION");
        System.out.println("=".repeat(63));
        System.out.println();

        // --- Sender Account ---
        Account senderAccount;
        String senderAccountNumber;
        while (true) {
            senderAccountNumber = ValidationUtils.getValidAccountNumber("user");
            if (senderAccountNumber == null || senderAccountNumber.trim().isEmpty()) {
                System.out.println("Account number cannot be empty. Enter again or type 'exit' to cancel.");
                continue;
            }
            if (senderAccountNumber.equalsIgnoreCase("exit")) return null;

            try {
                senderAccount = accountManager.findAccount(senderAccountNumber);
                break;
            } catch (Exception e) {
                System.out.println("Account not found. Try again!");
            }
        }

        // --- Display account details ---
        System.out.println("\nACCOUNT DETAILS");
        System.out.println("Customer: " + senderAccount.getCustomer().getName());
        System.out.println("Account Type: " + senderAccount.getAccountType());
        double previousBalance = senderAccount.getBalance();
        System.out.printf("Current Balance: $%,.2f\n", previousBalance);
        System.out.println();

        // --- Transaction Type ---
        String transactionType;
        while (true) {
            System.out.println("Transaction type:");
            System.out.println("1. Deposit");
            System.out.println("2. Withdrawal");
            System.out.println("3. Transfer");
            int choice = InputUtils.readInt("Select type (1-3): ");
            if (choice < 1 || choice > 3) {
                System.out.println("Invalid transaction type. Try again.");
                continue;
            }
            transactionType = choice == 1 ? "Deposit" : choice == 2 ? "Withdrawal" : "Transfer";
            break;
        }

        // --- Recipient account for Transfer ---
        String recipientAccountNumber = null;
        if ("Transfer".equalsIgnoreCase(transactionType)) {
            Account recipientAccount = null;
            while (true) {
                recipientAccountNumber = ValidationUtils.getValidAccountNumber("transfer");

                if (recipientAccountNumber == null || recipientAccountNumber.trim().isEmpty()) {
                    System.out.println("Recipient account cannot be empty. Enter again or type 'exit' to cancel.");
                    continue;
                }
                if (recipientAccountNumber.equalsIgnoreCase("exit")) return null;

                // Prevent sending to the same account
                if (recipientAccountNumber.equalsIgnoreCase(senderAccountNumber)) {
                    System.out.println("You cannot send to the same account. Enter a different recipient account.");
                    continue;
                }

                try {
                    recipientAccount = accountManager.findAccount(recipientAccountNumber);
                    break;
                } catch (Exception e) {
                    System.out.println("Recipient account not found. Try again!");
                    continue;
                }
            }
        }

        // --- Transaction Amount ---
        double amount;
        while (true) {
            amount = ValidationUtils.getValidAmount("Enter Amount: ");
            if (amount <= 0) {
                System.out.println("Amount must be greater than zero.");
                continue;
            }

            // For withdrawals or transfers, check minimum balance
            double resultingBalance = "Deposit".equalsIgnoreCase(transactionType) ?
                    previousBalance + amount : previousBalance - amount;


            if (senderAccount.getAccountType().equalsIgnoreCase("savingsAccount") && resultingBalance < SavingsAccount.getMinimumBalance() &&
                    !"Deposit".equalsIgnoreCase(transactionType)) {
                System.out.printf("Amount too high. Transaction would violate minimum balance of $%,.2f. Enter a different amount.\n",
                        SavingsAccount.getMinimumBalance());
                continue; // ask again
            }

            break; // valid amount
        }

        // --- New Balance Calculation ---
        double newBalance = "Deposit".equalsIgnoreCase(transactionType) ?
                previousBalance + amount : previousBalance - amount;

        // --- Confirmation ---
        System.out.println("\nTRANSACTION CONFIRMATION");
        System.out.println("-".repeat(63));
        System.out.println("Account: " + senderAccountNumber);
        System.out.println("Type: " + transactionType.toUpperCase());
        if ("Transfer".equalsIgnoreCase(transactionType)) {
            System.out.println("Recipient: " + recipientAccountNumber);
        }
        System.out.printf("Amount: $%,.2f\n", amount);
        System.out.printf("Previous Balance: $%,.2f\n", previousBalance);
        System.out.printf("New Balance: $%,.2f\n", newBalance);
        System.out.println("-".repeat(63));

        boolean confirm = InputUtils.readYesNo("Confirm transaction? (Y/N): ");
        if (!confirm) {
            System.out.println("Transaction cancelled.");
            return null;
        }else{

            InputUtils.show("processing Transaction...", 3);                
             TransactionRequest request = new TransactionRequest(senderAccountNumber, recipientAccountNumber, transactionType, amount);
            System.out.println("Transaction Completed Successfully");
            return request;
        }

    }
}
