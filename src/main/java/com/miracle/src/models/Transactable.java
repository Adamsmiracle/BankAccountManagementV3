package com.miracle.src.models;

public interface Transactable {

//    method to be implemented by other classes.
    public boolean processTransaction(double amount, String type);
}
