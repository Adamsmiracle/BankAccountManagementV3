package com.miracle.src.utils;

import com.miracle.src.models.Account;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrencyUtils {
    private static final int THREAD_COUNT = 10;

    public static void runConcurrentTransactions(Account account) {
        // Create a new ExecutorService for each test run
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        try {
            final double initialBalance = account.getBalance();
            System.out.println("=== Starting Concurrency Test ===");
            System.out.printf("Initial balance: $%,.2f%n", initialBalance);

            // Each thread will deposit and then withdraw the same amount
            final double transactionAmount = 100.0;
            final CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

            // Track successful operations
            AtomicInteger successfulOps = new AtomicInteger();
            AtomicInteger failedOps = new AtomicInteger();

            // Create and submit tasks
            for (int i = 0; i < THREAD_COUNT; i++) {
                executor.submit(() -> {
                    try {
                        // Each thread does one deposit and one withdrawal
                        account.deposit(transactionAmount);
                        System.out.printf("%s: Deposited $%,.2f%n",
                                Thread.currentThread().getName(), transactionAmount);

                        account.withdraw(transactionAmount);
                        System.out.printf("%s: Withdrew $%,.2f%n",
                                Thread.currentThread().getName(), transactionAmount);

                        successfulOps.getAndIncrement();
                    } catch (Exception e) {
                        failedOps.getAndIncrement();
                        System.out.printf("%s: Operation failed - %s%n",
                                Thread.currentThread().getName(), e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // Wait for all tasks to complete
            latch.await(5, TimeUnit.SECONDS);

            // Print results
            double finalBalance = account.getBalance();
            System.out.println("\n=== Test Results ===");
            System.out.printf("Initial balance: $%,.2f%n", initialBalance);
            System.out.printf("Final balance:   $%,.2f%n", finalBalance);
            System.out.printf("Expected balance: $%,.2f%n", initialBalance);
            System.out.printf("Successful operations: %d%n", successfulOps);
            System.out.printf("Failed operations: %d%n", failedOps);

            if (Math.abs(finalBalance - initialBalance) < 0.01) {
                System.out.println("✓ Test PASSED: Final balance matches expected");
            } else {
                System.out.println("✗ Test FAILED: Final balance does not match expected");
                System.out.printf("   Difference: $%,.2f%n",
                        Math.abs(finalBalance - initialBalance));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Test was interrupted");
        } finally {
            // Always shut down the executor
            executor.shutdownNow();
        }
    }
}