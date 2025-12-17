package com.miracle.src.utils;

import com.miracle.src.models.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrencyUtils {

    // Defines the batch size (number of transactions to simulate)
    private static final int TRANSACTION_COUNT = 10;

    /**
     * Runs concurrent deposit and withdrawal operations on the specified account
     * to test thread safety.
     *
     * @param account The account to perform concurrent transactions on
     */
    public static void runConcurrentTransactions(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }

        final String accountNumber = account.getAccountNumber();
        final double transactionAmount = 100.0;

        System.out.println("Running concurrent transaction simulation...");

        // Create ExecutorService with fixed thread pool
        ExecutorService executor = Executors.newFixedThreadPool(TRANSACTION_COUNT);

        // Use AtomicInteger for thread-safe counters
        AtomicInteger successfulOps = new AtomicInteger();
        AtomicInteger failedOps = new AtomicInteger();

        try {
            // Create a list to hold futures for tracking completion
            List<Future<?>> futures = new ArrayList<>();

            // Submit tasks to executor
            for (int i = 0; i < TRANSACTION_COUNT; i++) {
                final int threadNum = i + 1;
                Future<?> future = executor.submit(() -> {
                    try {
                        // Deposit
                        System.out.println("Thread-" + threadNum + ": Depositing $" + (int)transactionAmount + " to " + accountNumber);
                        account.deposit(transactionAmount);

                        // Withdraw
                        System.out.println("Thread-" + threadNum + ": Withdrawing $" + (int)transactionAmount + " from " + accountNumber);
                        account.withdraw(transactionAmount);

                        successfulOps.incrementAndGet();
                    } catch (Exception e) {
                        failedOps.incrementAndGet();
                        System.err.println("Thread-" + threadNum + " - Operation failed: " + e.getMessage());
                    }
                });
                futures.add(future);
            }

            // Wait for all tasks to complete
            for (Future<?> future : futures) {
                try {
                    future.get(); // Wait for this task to complete
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("Error waiting for task completion: " + e.getMessage());
                }
            }

            // Shutdown executor
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }

            // Print results
            System.out.println("\n√ Thread-safe operations completed successfully.");
            System.out.printf("Final Balance for %s: $%,.2f%n", accountNumber, account.getBalance());

        } catch (Exception e) {
            System.err.println("Fatal error during concurrency test: " + e.getMessage());
            e.printStackTrace();
            executor.shutdownNow();
        }
    }
}