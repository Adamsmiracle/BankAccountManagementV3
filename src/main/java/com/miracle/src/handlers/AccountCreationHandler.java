package com.miracle.src.handlers;

import com.miracle.src.services.AccountManager;
import com.miracle.src.dto.AccountRequest;
import com.miracle.src.models.exceptions.InvalidAmountException;
import com.miracle.src.models.exceptions.OverdraftExceededException;
import com.miracle.src.utils.AccountCreationInput;

/**
 * Handles the creation of new bank accounts by interacting with
 * the user input and delegating the account creation logic to AccountManager.
 */
public class AccountCreationHandler {

    /**
     * Singleton instance of AccountManager used to perform account operations.
     */
    private final AccountManager accountManager = AccountManager.getInstance();

    /**
     * Default constructor for AccountCreationHandler.
     */
    public AccountCreationHandler() {
        // No special initialization required
    }

    /**
     * Handles the process of creating a new account.
     * <p>
     * This method collects account creation data from the user,
     * and then uses the AccountManager to create the account.
     * </p>
     *
     * @throws InvalidAmountException if the initial deposit amount is invalid.
     * @throws OverdraftExceededException if an overdraft limit is exceeded during account creation.
     */
    public void handleCreateAccount() throws InvalidAmountException, OverdraftExceededException {
        AccountRequest request = AccountCreationInput.collectAccountCreationData();
        accountManager.createAccount(request);
    }
}
