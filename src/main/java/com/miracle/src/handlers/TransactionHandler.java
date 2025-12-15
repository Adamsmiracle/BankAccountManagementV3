package com.miracle.src.handlers;

import com.miracle.src.models.exceptions.AccountNotFoundException;
import com.miracle.src.models.exceptions.InsufficientFundsException;
import com.miracle.src.services.AccountManager;
import com.miracle.src.dto.TransactionRequest;
import com.miracle.src.models.exceptions.InvalidAmountException;
import com.miracle.src.models.exceptions.OverdraftExceededException;
import com.miracle.src.utils.TransactionProcessingInput;

/**
 * Handles processing of transactions such as deposits, withdrawals, and transfers.
 * This class collects transaction input from the user and delegates the transaction
 * processing to the AccountManager. It handles exceptions that may occur during
 * the transaction process and provides feedback to the user.
 */

public class TransactionHandler {

    private static AccountManager manager = AccountManager.getInstance();

    public TransactionHandler() {
    }

    /**
     * Handles a user-initiated transaction.
     * This method collects transaction details from the user, processes the transaction
     * via AccountManager, and handles exceptions such as invalid amounts, overdraft
     * issues, insufficient funds, or account not found errors. It provides appropriate
     * console feedback for success or failure of the transaction.
     */
    public void handleTransaction() {
        try {
            TransactionRequest request = TransactionProcessingInput.processTransactionMain();

            if (request == null) {
                return;
            }

            manager.processTransaction(request);
            // System.out.println("✔ Transaction processed successfully!");

        } catch (InvalidAmountException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        } catch (OverdraftExceededException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        } catch (AccountNotFoundException e) {
            System.out.println("Transaction failed: Account not found.");
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
