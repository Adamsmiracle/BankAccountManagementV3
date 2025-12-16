package com.miracle.src.services;
import com.miracle.src.models.*;
import com.miracle.src.models.exceptions.AccountNotFoundException;
import com.miracle.src.utils.InputUtils;

public class StatementGenerator {
    private static TransactionManager transactionManager = TransactionManager.getInstance();
    private static final AccountManager accountManager = AccountManager.getInstance();

    public StatementGenerator(TransactionManager transactionManager) {
        StatementGenerator.transactionManager = transactionManager;
    }



    public static void viewAllTransactionByAccount(String accountNumber) {

        try {
            transactionManager.sortTransactions();

            Account account = accountManager.findAccount(accountNumber);

            if (account == null) {
                throw new AccountNotFoundException(accountNumber);
            }

            System.out.println("\nACCOUNT STATEMENT");
            System.out.println("Account: " + account.getCustomer().getName() + " (" + account.getAccountType() + ")");
            System.out.printf("Current Balance: $%,.2f\n", account.getBalance());
            System.out.println("Transactions:");

            System.out.println("\nTRANSACTION HISTORY FOR ACCOUNT: " + accountNumber);
            System.out.println("=".repeat(85));
            System.out.printf("| %-6s | %-20s | %-13s | %-12s | %-10s |\n",
                    "ID", "TIMESTAMP", "TYPE", "AMOUNT", "BALANCE AFTER");
            System.out.println("-".repeat(85));

            boolean foundTransactions = false;

            double accountDeposits = 0.0;
            double accountWithdrawals = 0.0;
            double accountTransfersOuts = 0.0;
            double accountTransfersIns = 0.0;
            int matchedCount = 0;

            Transaction[] transactions = transactionManager.getTransactionsByAccount(accountNumber).toArray(new Transaction[0]);

            for (Transaction t : transactions) {


                if (t.getAccountNumber().equalsIgnoreCase(accountNumber)) {
                    System.out.printf("| %-6s | %-20s | %-13s | $%-11.2f | $%-9.2f |\n",
                            t.getTransactionId(),
                            t.getFormattedTimestamp(),
                            t.getType().toUpperCase(),
                            t.getAmount(),
                            t.getBalanceAfter()
                    );

                    matchedCount++;
                    foundTransactions = true;

                    if (t.getType().equalsIgnoreCase("DEPOSIT") || t.getType().equalsIgnoreCase("Transfer In")) {
                        accountDeposits += t.getAmount();
                    } else if (t.getType().equalsIgnoreCase("WITHDRAWAL") || t.getType().equalsIgnoreCase("Transfer Out")) {
                        accountWithdrawals += t.getAmount();
                    } else if (t.getType().equalsIgnoreCase("TRANSFER OUT")) {
                        accountTransfersOuts += t.getAmount();
                    } else if (t.getType().equalsIgnoreCase("TRANSFER IN")) {
                        accountTransfersIns += t.getAmount();
                    }
                }
            }

            System.out.println("-".repeat(85));

            if (!foundTransactions) {
                System.out.println("| No transactions found for this account.                                      |");
                System.out.println("-".repeat(85));
            } else {
                double net = accountDeposits + accountTransfersIns - accountWithdrawals - accountTransfersOuts;
                System.out.printf("\nTotal Transactions: %d\n", matchedCount);
                System.out.printf("Total Deposits/Transfers In: $%,.2f\n", accountDeposits);
                System.out.printf("Total Withdrawals/Transfers Out: $%,.2f\n", accountWithdrawals);
                System.out.printf("Net Change: $%,.2f\n", net);
            }

        } catch (AccountNotFoundException e) {
            System.out.println("\nERROR: Account was not found.");
        }
    }

    public static void displayAllTransactions() {
        transactionManager.sortTransactions();

        if (transactionManager.getTransactionCount() == 0) {
            System.out.println("\nNo transactions found.");
            return;
        }

        System.out.println("\nALL TRANSACTIONS");
        System.out.println("=".repeat(90));
        System.out.printf("| %-6s | %-12s | %-15s | %-15s | %-12s | %-12s |\n",
                "ID", "ACCOUNT", "TYPE", "TIMESTAMP", "AMOUNT", "BALANCE");
        System.out.println("-".repeat(90));

        for (int i = 0; i < transactionManager.getTransactionCount(); i++) {
            Transaction t = transactionManager.getTransaction(i);



            System.out.printf("| %-6s | %-12s | %-15s | %-15s | $%-11.2f | $%-11.2f |\n",
                    t.getTransactionId(),
                    t.getAccountNumber(),
                    t.getType(),
                    t.getFormattedTimestamp(),
                    t.getAmount(),
                    t.getBalanceAfter()
            );
        }

        System.out.println("=".repeat(90));
    }

    public static void displayAccountDetail(String accountNumber) {
        try {
            Account account = accountManager.findAccount(accountNumber);

            if (account == null) {
                throw new AccountNotFoundException(accountNumber);
            }

            System.out.println("\nACCOUNT DETAILS");
            System.out.println("=".repeat(40));
            System.out.println("Account Number : " + account.getAccountNumber());
            System.out.println("Account Type   : " + account.getAccountType());
            System.out.println("Customer Name  : " + account.getCustomer().getName());
            System.out.println("Customer Age   : " + account.getCustomer().getAge());
            System.out.println("Contact        : " + account.getCustomer().getContact());
            System.out.println("Address        : " + account.getCustomer().getAddress());
            System.out.printf("Balance        : $%,.2f\n", account.getBalance());
            System.out.println("=".repeat(40));

        } catch (AccountNotFoundException e) {
            System.out.println("\nERROR: Account " + accountNumber + " not found.");
        }
    }


    public static Account generateStatement(String accountNumber) {
        try {
            transactionManager.sortTransactions();

            // Try to find account
            Account account = accountManager.findAccount(accountNumber);

            // Throw exception if account not found
            if (account == null) {
                throw new AccountNotFoundException(accountNumber);
            }

            System.out.println("\nACCOUNT STATEMENT");
            System.out.println("=".repeat(85));
            System.out.println("Account Holder : " + account.getCustomer().getName());
            System.out.println("Account Number : " + account.getAccountNumber());
            System.out.println("Account Type   : " + account.getAccountType());
            System.out.printf("Current Balance: $%,.2f\n", account.getBalance());
            System.out.println("=".repeat(85));

            System.out.printf("| %-6s | %-20s | %-15s | %-12s | %-14s |\n",
                    "ID", "TIMESTAMP", "TYPE", "AMOUNT", "BALANCE AFTER");
            System.out.println("-".repeat(85));

            boolean foundTransactions = false;
            double totalDeposits = 0;
            double totalWithdrawals = 0;
            int transactionCount = 0;

            // Sort transactions to display the most recent ones first
            Transaction[] transactions = transactionManager.getTransactionsByAccount(accountNumber)
                    .stream()
                    .sorted((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp())) // Sort in descending order
                    .toArray(Transaction[]::new);

            for (Transaction t : transactions) {
                System.out.printf("| %-6s | %-20s | %-15s | $%-11.2f | $%-12.2f |\n",
                        t.getTransactionId(),
                        t.getFormattedTimestamp(),
                        t.getType().toUpperCase(),
                        t.getAmount(),
                        t.getBalanceAfter()
                );

                foundTransactions = true;
                transactionCount++;

                if (t.getType().equalsIgnoreCase("DEPOSIT") ||
                        t.getType().equalsIgnoreCase("TRANSFER IN")) {
                    totalDeposits += t.getAmount();
                } else if (t.getType().equalsIgnoreCase("WITHDRAWAL") ||
                        t.getType().equalsIgnoreCase("TRANSFER OUT")) {
                    totalWithdrawals += t.getAmount();
                }
            }

            System.out.println("-".repeat(85));

            if (!foundTransactions) {
                System.out.println("| No transactions found for this account.                                  |");
                System.out.println("=".repeat(85));
            } else {
                double net = totalDeposits +  - totalWithdrawals;

                System.out.println("\nSUMMARY");
                System.out.println("-".repeat(40));
                System.out.printf("Total Transactions       : %d\n", transactionCount);
                System.out.printf("Total Deposits/In        : $%,.2f\n", totalDeposits);
                System.out.printf("Total Withdrawals/Out    : $%,.2f\n", totalWithdrawals);
                System.out.printf("Net Change               : $%,.2f\n", net);
                System.out.println("=".repeat(40));
            }

            return account;

        } catch (AccountNotFoundException e) {
            System.out.println("Account not found\n");
            return null;
        }
    }

    public static void requestAndGenerateStatement() {
        while (true) {
            System.out.print("\nEnter account number to view statement (or type '0' to return): ");
            String accountNumber = InputUtils.readLine("> ");

            if (accountNumber.equalsIgnoreCase("0")) {
                System.out.println("Returning to main menu...");
                return; // Exit the method to return to the main menu
            }

            // Attempt to generate statement
            Account account = generateStatement(accountNumber);

            if (account != null) {
                break;
            }
        }
    }



}