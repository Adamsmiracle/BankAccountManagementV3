// src/main/java/com/miracle/src/services/TransactionContext.java
package com.miracle.src.services;

import com.miracle.src.models.Account;
import com.miracle.src.models.Transaction;

import java.util.HashMap;
import java.util.Map;

public class TransactionContext {
    private final Map<String, AccountState> accountStates = new HashMap<>();
    private Transaction transaction;
    private boolean completed = false;

    public void begin(Account... accounts) {
        for (Account account : accounts) {
            if (account != null && !accountStates.containsKey(account.getAccountNumber())) {
                accountStates.put(account.getAccountNumber(),
                        new AccountState(account, account.getBalance()));
            }
        }
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void rollback() {
        if (!completed) {
            for (AccountState state : accountStates.values()) {
                state.restore();
            }
        }
    }

    public void commit() {
        completed = true;
        accountStates.clear();
    }

    public boolean isCompleted() {
        return completed;
    }

    // Helper class to store account state
    private static class AccountState {
        private final Account account;
        private final double originalBalance;

        public AccountState(Account account, double originalBalance) {
            this.account = account;
            this.originalBalance = originalBalance;
        }

        public void restore() {
            account.setBalance(originalBalance);
        }
    }
}
