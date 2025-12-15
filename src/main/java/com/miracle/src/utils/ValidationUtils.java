package com.miracle.src.utils;

import java.util.Scanner;
import java.util.regex.Pattern;

import com.miracle.src.utils.InputUtils.*;

public final class ValidationUtils {



    public static String getValidatedInput(String prompt, String regexString, String errorMessage) {

//        Compile the regular expression for optimization
        final Pattern pattern = Pattern.compile(regexString);
        String input;
        while (true) {
            input = InputUtils.readLine(prompt);

            // Check for empty or null input.
            if (input == null || input.trim().isEmpty()) {
                System.out.println("Input cannot be empty. Please try again.");
                continue;
            }

            // Perform the Regex Validation Check
            if (pattern.matcher(input).matches()) {
                return input; // Success!
            } else {
                System.out.println("Invalid format. " + errorMessage);
            }
        }
    }


    public static int getValidAgeInput(String prompt, String errorMessage){
        int age;
        while (true){
                age = InputUtils.readInt(prompt);
                if (age > 0 && age < 120) {
                    return age;
                }
            System.out.println(errorMessage);
        }
    }

    public static String getValidAccountNumber(String transfer) {
        final String ACCOUNT_REGEX = "^ACC\\d{3}$";

        while (true) {
            String accountNumber;

            if (transfer.equalsIgnoreCase("transfer")) {
                accountNumber = InputUtils.readLine("Enter the recipient's Account Number (or type 'exit' to cancel): ").trim().toUpperCase();
            } else {
                accountNumber = InputUtils.readLine("Enter User's Account Number (or type '0' to cancel): ").trim().toUpperCase();
            }

            // Allow the user to exit
            if (accountNumber.equalsIgnoreCase("0")) {
                return "exit";  // return null if user wants to cancel
            }

            // Validate format
            if (accountNumber.matches(ACCOUNT_REGEX)) {
                return accountNumber;
            }

            System.out.println("Invalid account format. Account number must be ACC followed by three digits.");
        }
    }



    // In ValidationUtils.java
    public static double getValidAmount(String promptMessage) {
        while (true) {
            try {
                String input = InputUtils.readLine(promptMessage);
                double amount = Double.parseDouble(input);

                if (amount <= 0) {
                    System.out.println("Amount must be positive. Please try again.");
                    continue;
                }

                return amount;  // Valid amount, return it

            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please enter a valid number.");
            }
        }
    }


    // In ValidationUtils.java
    public static double getValidAmount(String promptMessage, double minAmount, double maxAmount) {
        while (true) {
            try {
                String input = InputUtils.readLine(promptMessage);
                double amount = Double.parseDouble(input);

                if (amount < minAmount) {
                    System.out.printf("Amount must be at least $%.2f. Please try again.\n", minAmount);
                    continue;
                }

                if (maxAmount > 0 && amount > maxAmount) {
                    System.out.printf("Amount cannot exceed $%.2f. Please try again.\n", maxAmount);
                    continue;
                }

                return amount;

            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please enter a valid number.");
            }
        }
    }


    public static double getValidAmount(String promptMessage, double minAmount) {
        while (true) {
            try {
                String input = InputUtils.readLine(promptMessage);
                double amount = Double.parseDouble(input);

                if (amount < minAmount) {
                    System.out.printf("Initial deposit must be at least $%.2f. Please try again.\n", minAmount);
                    continue;
                }
                return amount;

            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please enter a valid number.");
            }
        }
    }





}
