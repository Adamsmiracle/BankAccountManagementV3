package com.miracle.src.utils;

import com.miracle.src.models.Transaction;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionalUtils {


    public static List<Transaction> sortTransactionsByAmount(List<Transaction> transactions) {
        if (transactions.size()==1)
            return transactions;
        return transactions.stream()
                .sorted(Comparator.comparing(Transaction::getAmount))
                .collect(Collectors.toList());
    }


    public static List<Transaction> sortTransactionsByIdDescending(List<Transaction> transactions) {
        if (transactions == null || transactions.size() <= 1) {
            return transactions;
        }

        return transactions.stream()
                .sorted((t1, t2) -> {
                    try {
                        // Extract numeric parts of the IDs
                        int id1 = Integer.parseInt(t1.getTransactionId().replace("TXN", ""));
                        int id2 = Integer.parseInt(t2.getTransactionId().replace("TXN", ""));
                        return Integer.compare(id2, id1); // Descending order
                    } catch (NumberFormatException e) {
                        // Fallback to string comparison if parsing fails
                        return t2.getTransactionId().compareTo(t1.getTransactionId());
                    }
                })
                .collect(Collectors.toList());
    }

    
    public static List<Transaction> sortTransactionsByDate(List<Transaction> transactions) {
        return transactions.stream()
                .sorted(Comparator.comparing(Transaction::getTimestamp).reversed())
                .collect(Collectors.toList());
    }
}