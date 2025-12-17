package com.miracle.src.models;

import java.io.Serializable;

public class RegularCustomer extends Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    public RegularCustomer(String name, int age, String contact, String address) {
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
        return "Regular";
    }
}
