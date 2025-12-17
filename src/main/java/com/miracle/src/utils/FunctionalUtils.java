package com.miracle.src.utils;

import com.miracle.src.models.Transaction;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FunctionalUtils {

    public static List<Transaction> filterTransactions(List<Transaction> transactions, Predicate<Transaction> condition) {
        return transactions.stream()
                .filter(condition)
                .collect(Collectors.toList());
    }


    public static List<Transaction> sortTransactionsByAmount(List<Transaction> transactions) {
        return transactions.stream()
                .sorted(Comparator.comparing(Transaction::getAmount))
                .collect(Collectors.toList());
    }

    
    public static List<Transaction> sortTransactionsByDate(List<Transaction> transactions) {
        return transactions.stream()
                .sorted(Comparator.comparing(Transaction::getTimestamp))
                .collect(Collectors.toList());
    }
}