package com.miracle.src.utils;

import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.function.Predicate;

import com.miracle.src.utils.InputUtils.*;

public final class ValidationUtils {

    // Precompiled patterns for performance and reuse
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^ACC\\d{3}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_-]+@[A-Za-z0-9.-]+$");



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
            Matcher matcher = ACCOUNT_PATTERN.matcher(accountNumber);
            if (matcher.matches()) {
                return accountNumber;
            }

            System.out.println("Invalid account format. Account number must be ACC followed by three digits.");
        }
    }


    /**
     * Prompts for and validates an email address using Pattern/Matcher.
     * Regex: ^[A-Za-z0-9+_-]+@[A-Za-z0-9.-]+$
     */
    public static String getValidEmail(String prompt) {
        while (true) {
            String email = InputUtils.readLine(prompt);
            if (email == null || email.trim().isEmpty()) {
                System.out.println("Input cannot be empty. Please try again.");
                continue;
            }
            Matcher matcher = EMAIL_PATTERN.matcher(email.trim());
            if (matcher.matches()) {
                return email.trim();
            }
            System.out.println("Invalid email format. Please enter a valid email (e.g., user@example.com).");
        }
    }


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
}
