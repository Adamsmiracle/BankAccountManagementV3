package com.miracle.src.handlers;

import com.miracle.src.models.exceptions.AccountNotFoundException;
import com.miracle.src.models.exceptions.InsufficientFundsException;
import com.miracle.src.services.AccountManager;
import com.miracle.src.dto.TransactionRequest;
import com.miracle.src.models.exceptions.InvalidAmountException;
import com.miracle.src.models.exceptions.OverdraftExceededException;
import com.miracle.src.utils.TransactionProcessingInput;

import java.util.Optional;

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
    public void handleTransaction() throws InsufficientFundsException {
        try {
            TransactionRequest request = TransactionProcessingInput.processTransactionMain();
            if (request == null) {
                return;
            }

            manager.processTransaction(request);
            System.out.println("✔ Transaction processed successfully!");

        } catch (InvalidAmountException e) {
            System.out.println("\n Transaction failed: " + e.getMessage());
            System.out.println("   Please enter a valid positive amount.");
        } catch (OverdraftExceededException e) {
            System.out.println("\n Transaction failed: " + e.getMessage());
            System.out.println("   Please try a smaller amount or deposit funds first.");
        } catch (AccountNotFoundException e) {
            System.out.println("\n Transaction failed: Account not found.");
            System.out.println("   Please check the account number and try again.");
        } catch (RuntimeException e) {
            if (e.getCause() instanceof OverdraftExceededException) {
                System.out.println("\n Transaction failed: " + e.getCause().getMessage());
                System.out.println("   Please try a smaller amount or deposit funds first.");
            } else {
                System.out.println("\n An unexpected error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("\n An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
