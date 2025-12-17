package com.miracle.src.utils;

import com.miracle.src.models.Account;
import java.util.stream.IntStream;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrencyUtils {

    // Defines the batch size (number of transactions to simulate)
    private static final int TRANSACTION_COUNT = 10;

    public static void runConcurrentTransactions(Account account) {
        final double initialBalance = account.getBalance();
        final double transactionAmount = 100.0;

        // Use AtomicInteger to safely track shared results across parallel threads
        AtomicInteger successfulOps = new AtomicInteger();
        AtomicInteger failedOps = new AtomicInteger();

        System.out.println("=== Starting Parallel Stream Concurrency Test ===");
        System.out.printf("Initial balance: $%,.2f%n", initialBalance);

        // 1. Create a stream of indices (representing the transactions)
        IntStream.range(0, TRANSACTION_COUNT)
                .parallel()
                .forEach(i -> {
                    try {
                        // Each thread runs this block: one deposit and one withdrawal
                        account.deposit(transactionAmount);
                        account.withdraw(transactionAmount);

                        successfulOps.getAndIncrement();
                    } catch (Exception e) {
                        failedOps.getAndIncrement();
                        // System.out.printf("Operation failed - %s%n", e.getMessage());
                    }
                });

        // Print results
        double finalBalance = account.getBalance();
        System.out.println("\n=== Test Results ===");
        System.out.printf("Initial balance: $%,.2f%n", initialBalance);
        System.out.printf("Final balance:   $%,.2f%n", finalBalance);
        System.out.printf("Expected balance: $%,.2f%n", initialBalance);

        System.out.printf("Successful operations: %d%n", successfulOps.get());
        System.out.printf("Failed operations: %d%n", failedOps.get());

        if (Math.abs(finalBalance - initialBalance) < 0.01) {
            System.out.println("✓ Test PASSED: Final balance matches expected");
        } else {
            System.out.println("✗ Test FAILED: Final balance does not match expected");
            System.out.printf("   Difference: $%,.2f%n",
                    Math.abs(finalBalance - initialBalance));
        }
    }
}