// src/main/java/com/miracle/src/models/exceptions/TransactionFailedException.java
package com.miracle.src.models.exceptions;

public class TransactionFailedException extends Exception {
    public TransactionFailedException(String message) {
        super(message);
    }

    public TransactionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}