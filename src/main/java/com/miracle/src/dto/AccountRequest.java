package com.miracle.src.dto;

public class AccountRequest {

    private String name;
    private int age;
    private String contact;
    private String address;
    private int customerType;
    private int accountType;
    private double initialDeposit;

    public AccountRequest(String name, int age, String contact, String address, int customerType, int accountType, double initialDeposit) {
        this.name = name;
        this.age = age;
        this.contact = contact;
        this.address = address;
        this.customerType = customerType;
        this.accountType = accountType;
        this.initialDeposit = initialDeposit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getCustomerType() {
        return customerType;
    }

    public void setCustomerType(int customerType) {
        this.customerType = customerType;
    }

    public int getAccountType() {
        return accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public double getInitialDeposit() {
        return initialDeposit;
    }

    public void setInitialDeposit(double initialDeposit) {
        this.initialDeposit = initialDeposit;
    }
}
