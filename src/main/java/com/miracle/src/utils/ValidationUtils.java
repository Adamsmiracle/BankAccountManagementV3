package com.miracle.src.utils;

import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.function.Predicate;

import com.miracle.src.utils.InputUtils.*;

public final class ValidationUtils {

    // Precompiled patterns for performance and reuse
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^ACC\\d{3}$");

    // Email pattern: standard email format validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    // Phone number pattern: supports formats like (123) 456-7890, 123-456-7890, 1234567890, +1234567890
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+\\d{1,3})?[-.\\s]?\\(?\\d{1,4}\\)?[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,9}$"
    );

    // ========== Predicate-based Validation Rules ==========

    /**
     * Predicate for validating account numbers (ACC followed by 3 digits)
     */
    public static final Predicate<String> isValidAccountNumber =
            input -> input != null && ACCOUNT_PATTERN.matcher(input).matches();

    /**
     * Predicate for validating email addresses
     */
    public static final Predicate<String> isValidEmail =
            input -> input != null && EMAIL_PATTERN.matcher(input).matches();

    /**
     * Predicate for validating phone numbers
     */
    public static final Predicate<String> isValidPhoneNumber =
            input -> input != null && PHONE_PATTERN.matcher(input).matches();

    /**
     * Predicate for validating non-empty strings
     */
    public static final Predicate<String> isNotEmpty =
            input -> input != null && !input.trim().isEmpty();

    /**
     * Predicate for validating positive amounts
     */
    public static final Predicate<Double> isPositiveAmount =
            amount -> amount != null && amount > 0;

    /**
     * Predicate for validating valid age (18-120)
     */
    public static final Predicate<Integer> isValidAge =
            age -> age != null && age > 18 && age < 120;


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
                System.out.println("✓ Input accepted!");
                return input; // Success!
            } else {
                System.out.println("Error: "+ errorMessage);
            }
        }
    }


    public static int getValidAgeInput(String prompt, String errorMessage){
        int age;
        while (true){
                age = InputUtils.readInt(prompt);
                if (age > 18 && age < 120) {
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

    /**
     * Validates an email address using the predefined EMAIL_PATTERN.
     * @param email the email to validate
     * @return true if the email is valid, false otherwise
     */
    public static boolean validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return isValidEmail.test(email);
    }

    /**
     * Gets a validated email from user input.
     * @param prompt the prompt to display
     * @return a valid email address
     */
    public static String getValidEmail(String prompt) {
        while (true) {
            String input = InputUtils.readLine(prompt);

            if (input == null || input.trim().isEmpty()) {
                System.out.println("Error: Email cannot be empty. Please try again.");
                continue;
            }

            if (isValidEmail.test(input)) {
                System.out.println("✓ Email accepted!");
                return input;
            } else {
                System.out.println("Error: Invalid email format. Please enter a valid email (e.g., user@example.com).");
            }
        }
    }

    /**
     * Validates a phone number using the predefined PHONE_PATTERN.
     * @param phone the phone number to validate
     * @return true if the phone number is valid, false otherwise
     */
    public static boolean validatePhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return isValidPhoneNumber.test(phone);
    }

    /**
     * Gets a validated phone number from user input.
     * @param prompt the prompt to display
     * @return a valid phone number
     */
    public static String getValidPhoneNumber(String prompt) {
        while (true) {
            String input = InputUtils.readLine(prompt);

            if (input == null || input.trim().isEmpty()) {
                System.out.println("Error: Phone number cannot be empty. Please try again.");
                continue;
            }

            if (isValidPhoneNumber.test(input)) {
                System.out.println("✓ Phone number accepted!");
                return input;
            } else {
                System.out.println("Error: Invalid phone format. Please enter a valid phone number (e.g., 123-456-7890 or +1234567890).");
            }
        }
    }

    /**
     * Generic validation method using Predicate lambdas for dynamic validation rules.
     * @param input the input to validate
     * @param validator the Predicate to use for validation
     * @param errorMessage the error message to display if validation fails
     * @return true if input passes validation
     */
    public static <T> boolean validateWithPredicate(T input, Predicate<T> validator, String errorMessage) {
        if (validator.test(input)) {
            return true;
        }
        System.out.println("Error: " + errorMessage);
        return false;
    }

    /**
     * Gets validated input using a custom Predicate validator.
     * @param prompt the prompt to display
     * @param validator the Predicate to use for validation
     * @param errorMessage the error message to display if validation fails
     * @return the validated input string
     */
    public static String getValidatedInputWithPredicate(String prompt, Predicate<String> validator, String errorMessage) {
        while (true) {
            String input = InputUtils.readLine(prompt);

            if (input == null || input.trim().isEmpty()) {
                System.out.println("Error: Input cannot be empty. Please try again.");
                continue;
            }

            if (validator.test(input)) {
                System.out.println("✓ Input accepted!");
                return input;
            } else {
                System.out.println("Error: " + errorMessage);
            }
        }
    }
}
