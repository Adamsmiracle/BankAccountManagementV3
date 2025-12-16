package com.miracle.src.utils;

import com.miracle.src.models.Account;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FileIOUtils {

    private static final String ACCOUNTS_FILE = "accounts.dat";

    public static void saveAccounts(Map<String, Account> accounts) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ACCOUNTS_FILE))) {
            oos.writeObject(accounts);
        }
    }

    public static Map<String, Account> loadAccounts() throws IOException, ClassNotFoundException {
        File file = new File(ACCOUNTS_FILE);
        if (!file.exists()) {
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<String, Account>) ois.readObject();
        }
    }
}