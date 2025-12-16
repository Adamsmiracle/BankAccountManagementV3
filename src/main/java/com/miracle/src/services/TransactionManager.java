package com.miracle.src.services;

import com.miracle.src.models.Account;
import com.miracle.src.models.Transaction;
import com.miracle.src.models.exceptions.TransactionFailedException;
import com.miracle.src.utils.FunctionalUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

public class TransactionManager {
    private static final TransactionManager INSTANCE = new TransactionManager();
    private final ThreadLocal<Stack<TransactionContext>> transactionStack =
            ThreadLocal.withInitial(Stack::new);
    private final int MAX_TRANSACTION = 200;
    private List<Transaction> transactions = new ArrayList<>(MAX_TRANSACTION);
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
        transactions.add(transaction);
    }


    public List<Transaction> getTransactionsByAccount(String accountNumber) {
        return FunctionalUtils.filterTransactions(transactions, 
            t -> t.getAccountNumber().equalsIgnoreCase(accountNumber));
    }

    public void sortTransactions() {
        transactions.sort(Comparator.comparing(Transaction::getTimestamp).reversed());
    }

    public void sortTransactionsByAmount() {
        transactions = FunctionalUtils.sortTransactionsByAmount(transactions);
    }

    public void sortTransactionsByDate() {
        transactions = FunctionalUtils.sortTransactionsByDate(transactions);
    }


    // In TransactionManager.java
    public Transaction getTransaction(int index) {
        if (index < 0 || index >= transactionCount) {
            throw new IndexOutOfBoundsException("Invalid transaction index: " + index);
        }
        return transactions.get(index);
    }

    public int getTransactionCount() {
        return transactionCount;
    }
}