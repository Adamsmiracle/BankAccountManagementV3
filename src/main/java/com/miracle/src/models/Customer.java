package com.miracle.src.models;

import java.io.Serializable;

public abstract class Customer implements Serializable {

//    Static fields;
    public static int customerCounter = 0;

//    private fields
    private final String customerId;
    private String name;
    private int age;
    private String contact;
    private String address;

    public Customer(String name, int age, String contact, String address) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be null or empty");
        }
        if (age < 18 || age > 120) {
            throw new IllegalArgumentException("Customer age must be between 18 and 120");
        }
        if (contact == null || contact.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer contact cannot be null or empty");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer address cannot be null or empty");
        }

        this.name = name.trim();
        this.age = age;
        this.contact = contact.trim();
        this.address = address.trim();
        this.customerId = String.format("CUS%03d", ++customerCounter);
    }

    /**
     * Constructor for loading customer from file.
     * Preserves the original customer ID and updates the counter if needed.
     *
     * @param name customer name
     * @param age customer age
     * @param contact customer contact
     * @param address customer address
     * @param customerId the original customer ID to preserve
     * @param fromFile flag to indicate this is loaded from file
     */
    public Customer(String name, int age, String contact, String address, String customerId, boolean fromFile) {
        this.name = name.trim();
        this.age = age;
        this.contact = contact.trim();
        this.address = address.trim();
        this.customerId = customerId;
        // Update the counter to be at least as high as this customer ID
        try {
            if (customerId != null && customerId.startsWith("CUS")) {
                int num = Integer.parseInt(customerId.substring(3));
                if (num > customerCounter) {
                    customerCounter = num;
                }
            }
        } catch (NumberFormatException ignored) {
            // If parsing fails, just use the provided customer ID
        }
    }

//    SETTERS
    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setAddress(String address){
        this.address = address;
    }


//    GETTERS
    public String getName(){
        return this.name;
    }

    public int getAge() {
        return this.age;
    }

    public String getContact(){
        return this.contact;
    }

    public String getAddress() {
        return this.address;
    }

    public String getCustomerId(){
        return this.customerId;
    }

//    ABSTRACT METHODS
    abstract public void displayCustomerDetails();
    abstract public String getCustomerType();

}
