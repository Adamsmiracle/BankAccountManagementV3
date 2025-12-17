package com.miracle;
// Import all necessary packages
import com.miracle.src.handlers.AccountCreationHandler;
import com.miracle.src.handlers.TransactionHandler;
import com.miracle.src.models.Account;
import com.miracle.src.models.CheckingAccount;
import com.miracle.src.models.Customer;
import com.miracle.src.models.RegularCustomer;
import com.miracle.src.models.exceptions.AccountNotFoundException;
import com.miracle.src.models.exceptions.InvalidAmountException;
import com.miracle.src.models.exceptions.OverdraftExceededException;
import com.miracle.src.services.*;
import com.miracle.src.utils.ConcurrencyUtils;
import com.miracle.src.utils.FileIOUtils;
import com.miracle.src.utils.InputUtils;
import com.miracle.src.utils.ValidationUtils;
import com.miracle.src.services.*;

import javax.swing.plaf.synth.SynthOptionPaneUI;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import com.miracle.src.services.AccountManager;

import java.io.IOException;


public class Main {
//    private static AccountService accountService;
    private static final AccountManager accountManager = AccountManager.getInstance();
    private static final TransactionManager transactionManager = TransactionManager.getInstance();
    private static final AccountCreationHandler accountCreationHandler = new AccountCreationHandler();
    private static final TransactionHandler transactionHandler = new TransactionHandler();



    public static void main(String[] args) throws InterruptedException, InvalidAmountException, OverdraftExceededException, IOException {

        AccountManager.loadAccountsOnStart();
        TransactionManager.loadTransactionsOnStart();
        runMainMenu();


    }

    private static void runMainMenu() throws IOException {
        int choice;
        do {
            mainMenu();
            choice = InputUtils.readInt("Enter choice:> ");
            executeChoice(choice);
        } while (choice != 7);
    }

    private static void executeChoice(int choice) throws IOException {
        try {
            switch (choice) {
                case 1:
                    manageAccounts();
                    break;

                case 2:
                    transactionHandler.handleTransaction();
                    break;

                case 3:
                    generateReports();
                    break;

                case 4:
                    saveOrLoadData();
                    break;

                case  5:
                    concurrencyTest();
                    break;
                case  6:
                    runTest();

                case 7:
                    accountManager.saveAccountsOnExit();
                    transactionManager.saveTransactionsOnExit();
                    System.out.println("\nExiting application... \n");
                    return;

                default:
                    System.out.println("\nInvalid choice. Please select an option between 1 and 6.\n");
            }

        } catch (InvalidAmountException | OverdraftExceededException e) {
            System.out.println("\nERROR: " + e.getMessage());
        }

        if (choice != 7) {
            InputUtils.readLine("\nPress Enter to continue... ");
        }
    }



    public static void mainMenu(){
        System.out.println("\n\n"+"=".repeat(65));
        System.out.println("||                   BANK ACCOUNT MANAGEMENT                  ||");
        System.out.println("=".repeat(65));
        System.out.println("1. Manage Accounts");
        System.out.println("2. Perform Transactions");
        System.out.println("3. Generate Statements");
        System.out.println("4. Save/Load Data");
        System.out.println("5. Run Concurrent Simulations");
        System.out.println("6. Run test");
        System.out.println("7. Exit");
        System.out.println("\n");
    }


    public static void generateReports() {
        System.out.println("-".repeat(50));
        System.out.println("||            TRANSACTION HISTORY MENU          ||");
        System.out.println("-".repeat(50));
        System.out.println("1. View ALL Transactions");
        System.out.println("2. View Transactions By Account");
        System.out.println("3. View Account Details");
        System.out.println("4. Generate Bank Statment");
        System.out.println("5. View all customers");
        System.out.println("0. Back to Main Menu");
        System.out.println("\n");

        int choice = InputUtils.readInt("Enter choice:> ");

        switch (choice) {

            case 1:
                StatementGenerator.displayAllTransactions();
                break;

            case 2:
                String accountNumber = ValidationUtils.getValidAccountNumber("Enter Account Number:> ");
                StatementGenerator.viewAllTransactionByAccount(accountNumber);
                break;

            case 3:
                String accNum = ValidationUtils.getValidAccountNumber("Enter Account Number:> ");
                StatementGenerator.displayAccountDetail(accNum);
                break;
            case 4:
                StatementGenerator.requestAndGenerateStatement();
                break;
            case 5:
                accountManager.displayAllCustomers();
                break;
            case 0:
                System.out.println("Returning to main menu... ");
                return;

            default:
                System.out.println("Invalid selection. Please try again.");
        }
    }


    public static void manageAccounts() throws InvalidAmountException, OverdraftExceededException {
        while (true) {
            System.out.println("\n\n"+"-".repeat(65));
            System.out.println("||                   MANAGE ACCOUNTS                  ||");
            System.out.println("-".repeat(65));
            System.out.println("\n1. Create Account");
            System.out.println("2. View Accounts");
            System.out.println("3. Display All customers");

            int choice = InputUtils.readInt("Select option (1-3):> ");
            System.out.println("\n");

            if (choice == 1) {
                accountCreationHandler.handleCreateAccount();
                return; // exit method completely
            } else if (choice == 2) {
                accountManager.viewAllAccounts();
                return; // exit method completely
            } else if (choice == 3) {
                accountManager.displayAllCustomers();
                return; // exit method completely
            } else {
                System.out.println("Invalid input! Please enter a number between 1 and 3.\n");
            }
        }
    }


    public  static void saveOrLoadData() throws IOException {
        System.out.println("-".repeat(50));
        System.out.println("||            SAVE/LOAD DATA MENU          ||");
        System.out.println("-".repeat(50));
        System.out.println("1. Save data");
        System.out.println("2. Load data");
        System.out.println("0. Back to Main Menu");
        System.out.println("\n");


        int choice = InputUtils.readInt("Enter choice:> ");
        switch (choice){
            case 1:
                accountManager.saveAccountsOnExit();
                transactionManager.saveTransactionsOnExit();
                break;
            case 2:
                AccountManager.loadAccountsOnStart();
                TransactionManager.loadTransactionsOnStart();
        }

    }


    public static void runTest() {
        SummaryGeneratingListener listener = new SummaryGeneratingListener();

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectPackage("com.miracle.runner"))
                .build();


        Launcher launcher = LauncherFactory.create();
        launcher.execute(request, listener);

        TestExecutionSummary summary = listener.getSummary();
        summary.printTo(new java.io.PrintWriter(System.out));
    }




    public static void concurrencyTest() throws InvalidAmountException {
        Customer customer = new RegularCustomer("John", 88, "0555555555", "123 kumasi");
        Account account = new CheckingAccount(customer, 1000.0); // Starting with $1000
    ConcurrencyUtils.runConcurrentTransactions(account);

    // Make sure to call shutdown when done
    }


}