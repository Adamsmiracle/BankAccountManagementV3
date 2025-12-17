package com.miracle.src.models;

public class PremiumCustomer extends Customer {
    private static double minimumBalance = 10000.00;

    public PremiumCustomer(String name, int age, String contact, String address) {
        super(name, age, contact, address);
    }


   @Override
    public void displayCustomerDetails() {
        System.out.println("-".repeat(30));
        System.out.println("CUSTOMER DETAILS");
        System.out.println("ID: " + getCustomerId());
        System.out.println("Type: " + getCustomerType());
        System.out.println("Name: " + getName());
        System.out.println("Age: " + getAge());
        System.out.println("Contact: " + getContact());
        System.out.println("Address: " + getAddress());
        System.out.println("-".repeat(30));
    }

    @Override
    public String getCustomerType() {
        return "Premium";
    }

//    Premium customers don't pay monthly fees
    public boolean hasWaivedFees() {
        return true;
    }

    public static double getMinimumBalance() {
        return minimumBalance;
    }


}
