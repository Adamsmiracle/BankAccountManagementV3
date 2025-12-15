// In TransactionManager.java, replace the entire content with:
package com.miracle.src.services;

import com.miracle.src.models.Account;
import com.miracle.src.models.Transaction;
import com.miracle.src.models.exceptions.TransactionFailedException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

public class TransactionManager {
    private static final TransactionManager INSTANCE = new TransactionManager();
    private final ThreadLocal<Stack<TransactionContext>> transactionStack =
            ThreadLocal.withInitial(Stack::new);
    private final int MAX_TRANSACTION = 200;
    private final Transaction[] transactions = new Transaction[MAX_TRANSACTION];
    private int transactionCount = 0;

    public static TransactionManager getInstance() {
        return INSTANCE;
    }

    private TransactionManager() {}

    // Transaction Management Methods
    public void beginTransaction(Account... accounts) {
        TransactionContext context = new TransactionContext();
        context.begin(accounts);
        transactionStack.get().push(context);
    }

    public void commit() throws TransactionFailedException {
        TransactionContext context = getCurrentContext();
        try {
            context.commit();
            transactionStack.get().pop();
        } catch (Exception e) {
            rollback();
            throw new TransactionFailedException("Transaction commit failed", e);
        }
    }

    public void rollback() {
        if (!transactionStack.get().isEmpty()) {
            TransactionContext context = transactionStack.get().pop();
            if (context != null && !context.isCompleted()) {
                context.rollback();
            }
        }
    }

    public TransactionContext getCurrentContext() {
        if (transactionStack.get().isEmpty()) {
            throw new IllegalStateException("No active transaction");
        }
        return transactionStack.get().peek();
    }

    public boolean isInTransaction() {
        return !transactionStack.get().isEmpty();
    }

    // Transaction Storage Methods
    public void addTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }

        if (transactionCount >= transactions.length) {
            throw new IllegalStateException(
                    "Transaction Manager storage limit reached (" + MAX_TRANSACTION +
                            "). Cannot record new transaction."
            );
        }

        transactions[transactionCount++] = transaction;
    }

    public List<Transaction> getTransactionsByAccount(String accountNumber) {
        sortTransactions();
        return Arrays.stream(transactions, 0, transactionCount)
                .filter(t -> t != null && t.getAccountNumber().equalsIgnoreCase(accountNumber))
                .toList();
    }



    public void sortTransactions() {
        // Sort transactions by timestamp (most recent first)
        Arrays.sort(transactions, 0, transactionCount,
                Comparator.nullsLast(Comparator.comparing(Transaction::getTimestamp).reversed())
        );
    }

    // In TransactionManager.java
    public Transaction getTransaction(int index) {
        if (index < 0 || index >= transactionCount) {
            throw new IndexOutOfBoundsException("Invalid transaction index: " + index);
        }
        return transactions[index];
    }

    public int getTransactionCount() {
        return transactionCount;
    }
}