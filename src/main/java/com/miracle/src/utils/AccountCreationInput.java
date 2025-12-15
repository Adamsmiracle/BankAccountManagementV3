package com.miracle.src.utils;

import com.miracle.src.dto.AccountRequest;
import com.miracle.src.models.SavingsAccount;

import java.util.InputMismatchException;
import java.util.regex.PatternSyntaxException;

public class AccountCreationInput {

    private static final String NAME_REGEX = "^[A-Za-z]{2,}[A-Za-z'-]*(?:\\s[A-Za-z]{2,}[A-Za-z'-]*)+$";
    private static final String CONTACT_REGEX =
            "^(?:0(?:24|54|55|59|25|20|50|26|56|57|23)[\\s-]?\\d{3}[\\s-]?\\d{4}"
                    + "|(?:\\+233|233|00233)(?:24|54|55|59|25|20|50|26|56|57|23)[\\s-]?\\d{3}[\\s-]?\\d{4})$";
    private static final String ADDRESS_REGEX = "^[A-Za-z0-9][A-Za-z0-9\\s,.'\\-/#]{4,99}$";

    public static AccountRequest collectAccountCreationData() {

        try {

            String name = ValidationUtils.getValidatedInput(
                    "Enter customer name: ", NAME_REGEX,
                    "Enter at least FirstName and SurName"
            );

            int age = ValidationUtils.getValidAgeInput(
                    "Enter customer age: ",
                    "Age must be between 18 and 120"
            );

            String contact = ValidationUtils.getValidatedInput(
                    "Enter customer contact: ",
                    CONTACT_REGEX,
                    "Enter valid contact"
            );
            String address = ValidationUtils.getValidatedInput(
                    "Enter customer address: ",
                    ADDRESS_REGEX,
                    "Enter a valid address format (eg: 123 Oak Street, Springfield)"
            );

            System.out.println("\n");
            int customerType;
            while (true) {
                try {
                    System.out.println("Customer type: ");
                    System.out.println("1. Regular Customer (Standard banking services)");
                    System.out.println("2. Premium Customer (Enhanced benefits, min balance $10,000)");
                    customerType = InputUtils.readInt("Select customer type (1-2): ");

                    if (customerType == 1 || customerType == 2) break;

                    System.out.println("Invalid customer type\n");

                } catch (InputMismatchException | NumberFormatException e) {
                    System.out.println("Enter a valid number (1 or 2)");
                }
            }

            System.out.println("\n");

            int accountType;
            while (true) {
                try {
                    System.out.println("Account type: ");
                    System.out.println("1. Savings Account (Interest: 3.5%, Min balance: $500)");
                    System.out.println("2. Checking Account (Overdraft: $1000, Monthly fee: $10)");
                    accountType = InputUtils.readInt("Select account type (1-2): ");

                    if (accountType == 1 || accountType == 2) break;
                    System.out.println("Invalid account type\n");
                } catch (InputMismatchException | NumberFormatException e) {
                    System.out.println("Enter a valid number (1 or 2)");
                }
            }
            System.out.println("\n");

            double initialDeposit;
            while (true) {
                try {
                    initialDeposit = ValidationUtils.getValidAmount("Enter initial deposit: ");

                    if (customerType == 2 && initialDeposit < 10000.00) {
                        System.out.println("Premium customers require minimum initial deposit of $10,000.00\n");
                        continue;

                    }
                    else if (accountType == 1 && initialDeposit < SavingsAccount.getMinimumBalance()) {
                        System.out.println("Savings account requires minimum initial deposit of $500.00\n");
                        continue;
                    }
                    
                    break;
                } catch (InputMismatchException | NumberFormatException e) {
                    System.out.println("Enter a valid numeric amount\n");
                }
            }
            System.out.println("\n");

            return new AccountRequest(
                    name, age, contact, address,
                    customerType, accountType, initialDeposit
            );
        }
        catch (PatternSyntaxException e) {
            System.out.println("A system error occurred: invalid regex configuration.");
            e.printStackTrace();
        }
        catch (IllegalArgumentException e) {
            System.out.println("Invalid argument: " + e.getMessage());
        }
        catch (NullPointerException e) {
            System.out.println("A required value was null. Please try again.");
        }
        catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
