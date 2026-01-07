package com.miracle.src.utils;

import com.miracle.src.models.*;
import com.miracle.src.models.exceptions.InvalidAmountException;
import com.miracle.src.services.AccountManager;
import com.miracle.src.services.TransactionManager;

/**
 * Utility class for seeding initial data into the system.
 * Creates sample accounts and transactions for testing and demonstration purposes.
 */
public class DataSeeder {

    private static final AccountManager accountManager = AccountManager.getInstance();
    private static final TransactionManager transactionManager = TransactionManager.getInstance();
    private static boolean dataSeeded = false;

    private DataSeeder() {
        // Private constructor to prevent instantiation
    }

    /**
     * Seeds initial accounts and transactions if not already seeded.
     * Creates sample customers, accounts, and transactions for demonstration.
     */
    public static void seedInitialData() {
        if (dataSeeded) {
            System.out.println("Data has already been seeded.");
            return;
        }

        // Check if accounts already exist (loaded from file)
        if (accountManager.getAccountCount() > 0) {
            System.out.println("Existing data found. Skipping seed.");
            dataSeeded = true;
            return;
        }

        System.out.println("\n" + "=".repeat(50));
        System.out.println("       SEEDING INITIAL DATA");
        System.out.println("=".repeat(50));

        try {
            // Seed accounts
            seedAccounts();

            // Seed transactions
            seedTransactions();

            dataSeeded = true;

            System.out.println("\n✔ Initial data seeded successfully!");
            System.out.println("  - Accounts created: " + accountManager.getAccountCount());
            System.out.println("  - Transactions recorded: " + transactionManager.getTransactionCount());
            System.out.println("=".repeat(50) + "\n");

        } catch (Exception e) {
            System.err.println("✘ Error seeding data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Seeds sample accounts with different customer and account types.
     */
    private static void seedAccounts() throws InvalidAmountException {
        System.out.println("\nCreating sample accounts...");

        // Regular Customer with Savings Account
        Customer customer1 = new RegularCustomer(
                "John Mensah",
                35,
                "0241234567",
                "15 Independence Ave, Accra"
        );
        Account account1 = new SavingsAccount(customer1, 5000.00);
        accountManager.addAccount(account1);
        System.out.println("  ✓ Created: " + account1.getAccountNumber() + " - " + customer1.getName() + " (Savings)");

        // Regular Customer with Checking Account
        Customer customer2 = new RegularCustomer(
                "Ama Serwaa",
                28,
                "0551234567",
                "23 Oxford Street, Osu"
        );
        Account account2 = new CheckingAccount(customer2, 2500.00);
        accountManager.addAccount(account2);
        System.out.println("  ✓ Created: " + account2.getAccountNumber() + " - " + customer2.getName() + " (Checking)");

        // Premium Customer with Savings Account
        Customer customer3 = new PremiumCustomer(
                "Kwame Asante",
                45,
                "0201234567",
                "10 Liberation Road, Kumasi"
        );
        Account account3 = new SavingsAccount(customer3, 15000.00);
        accountManager.addAccount(account3);
        System.out.println("  ✓ Created: " + account3.getAccountNumber() + " - " + customer3.getName() + " (Premium Savings)");

        // Premium Customer with Checking Account
        Customer customer4 = new PremiumCustomer(
                "Efua Owusu",
                52,
                "0261234567",
                "5 High Street, Takoradi"
        );
        Account account4 = new CheckingAccount(customer4, 25000.00);
        accountManager.addAccount(account4);
        System.out.println("  ✓ Created: " + account4.getAccountNumber() + " - " + customer4.getName() + " (Premium Checking)");

        // Regular Customer with Savings Account (minimum balance)
        Customer customer5 = new RegularCustomer(
                "Kofi Adjei",
                22,
                "0541234567",
                "78 Ring Road, Tamale"
        );
        Account account5 = new SavingsAccount(customer5, 1000.00);
        accountManager.addAccount(account5);
        System.out.println("  ✓ Created: " + account5.getAccountNumber() + " - " + customer5.getName() + " (Savings)");
    }

    /**
     * Seeds sample transactions for the created accounts.
     */
    private static void seedTransactions() {
        System.out.println("\nRecording sample transactions...");

        try {
            // Get all accounts
            var accounts = accountManager.getAllAccounts().toArray(new Account[0]);

            if (accounts.length >= 5) {
                // Account 1 transactions (Savings)
                Account acc1 = accounts[0];
                acc1.deposit(1500.00);
                System.out.println("  ✓ Deposit: $1,500.00 to " + acc1.getAccountNumber());

                acc1.withdraw(500.00);
                System.out.println("  ✓ Withdrawal: $500.00 from " + acc1.getAccountNumber());

                // Account 2 transactions (Checking)
                Account acc2 = accounts[1];
                acc2.deposit(750.00);
                System.out.println("  ✓ Deposit: $750.00 to " + acc2.getAccountNumber());

                acc2.withdraw(200.00);
                System.out.println("  ✓ Withdrawal: $200.00 from " + acc2.getAccountNumber());

                // Account 3 transactions (Premium Savings)
                Account acc3 = accounts[2];
                acc3.deposit(5000.00);
                System.out.println("  ✓ Deposit: $5,000.00 to " + acc3.getAccountNumber());

                // Account 4 transactions (Premium Checking)
                Account acc4 = accounts[3];
                acc4.deposit(10000.00);
                System.out.println("  ✓ Deposit: $10,000.00 to " + acc4.getAccountNumber());

                acc4.withdraw(3000.00);
                System.out.println("  ✓ Withdrawal: $3,000.00 from " + acc4.getAccountNumber());

                // Transfer simulation (Account 4 to Account 1)
                if (acc4 instanceof CheckingAccount && acc1 instanceof SavingsAccount) {
                    acc4.withdrawWithType(2000.00, "Transfer Out");
                    acc1.depositWithType(2000.00, "Transfer In");
                    System.out.println("  ✓ Transfer: $2,000.00 from " + acc4.getAccountNumber() + " to " + acc1.getAccountNumber());
                }

                // Account 5 transactions (Regular Savings)
                Account acc5 = accounts[4];
                acc5.deposit(250.00);
                System.out.println("  ✓ Deposit: $250.00 to " + acc5.getAccountNumber());
            }

        } catch (Exception e) {
            System.err.println("  ✘ Error recording transaction: " + e.getMessage());
        }
    }

    /**
     * Forces re-seeding of data (useful for testing).
     * WARNING: This will add duplicate accounts if called multiple times.
     */
    public static void forceReseed() {
        dataSeeded = false;
        seedInitialData();
    }

    /**
     * Checks if data has been seeded.
     * @return true if data has been seeded
     */
    public static boolean isDataSeeded() {
        return dataSeeded;
    }

    /**
     * Displays a summary of seeded data.
     */
    public static void displaySeedSummary() {
        System.out.println("\n" + "-".repeat(50));
        System.out.println("SEED DATA SUMMARY");
        System.out.println("-".repeat(50));
        System.out.println("Total Accounts: " + accountManager.getAccountCount());
        System.out.println("Total Transactions: " + transactionManager.getTransactionCount());
        System.out.printf("Total Bank Balance: $%,.2f%n", accountManager.getTotalBalance());
        System.out.println("-".repeat(50));
    }
}

