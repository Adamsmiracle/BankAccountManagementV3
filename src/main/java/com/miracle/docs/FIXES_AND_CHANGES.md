# Bank Account Management System - Bug Fixes and Changes

**Date:** January 6, 2026  
**Version:** 3.1.1

---

## Summary of Issues Fixed

This document details all the bug fixes and improvements made to the Bank Account Management System.

---

## Issue 1: Transaction Saving and Loading from File

### Problem
- Transactions were not being properly saved to `transactions.txt`
- When loading transactions from file, the system would fail to persist them correctly
- The `newTransactions.clear()` was being called BEFORE `FileIOUtils.saveTransactionsToFile(snapshot)`, meaning if saving failed, transactions were lost

### Root Causes
1. The `readTransactionsFromFile()` method was checking against `TransactionManager.getInstance().getAllTransactions()` during load, causing circular dependency issues
2. The save logic cleared `newTransactions` before actually writing to disk
3. No proper distinction between "loaded from file" transactions and "newly created" transactions

### Solutions Applied

#### `TransactionManager.java` Changes:
```java
// Added volatile flag to track if data has been loaded
private static volatile boolean dataLoaded = false;

// Changed transactions list to be synchronized
private static final List<Transaction> transactions = Collections.synchronizedList(new ArrayList<>());

// New method for loading transactions without tracking them as "new"
public void addLoadedTransaction(Transaction transaction) {
    // Only adds to transactions list, NOT to newTransactions
}

// Fixed loadTransactionsOnStart() to NOT clear transactions before loading
// Only adds NEW transactions from file that aren't already in memory
public static void loadTransactionsOnStart() {
    List<Transaction> loaded = FileIOUtils.readTransactionsFromFile();
    // Only add transactions that were actually loaded (not already in memory)
    if (!loaded.isEmpty()) {
        synchronized (transactions) {
            for (Transaction t : loaded) {
                INSTANCE.addLoadedTransaction(t);
            }
        }
    }
    // ...
}

// Fixed saveTransactionsOnExit() to clear AFTER successful save
public void saveTransactionsOnExit() {
    // Now clears newTransactions only AFTER FileIOUtils.saveTransactionsToFile() succeeds
}
```

#### `FileIOUtils.java` Changes:
```java
// Fixed readTransactionsFromFile() to check against in-memory transactions
public static List<Transaction> readTransactionsFromFile() {
    // Gets existing transaction IDs from TransactionManager
    Set<String> existingIds = new HashSet<>();
    TransactionManager.getInstance().getAllTransactions().stream()
            .filter(t -> t != null && t.getTransactionId() != null)
            .forEach(t -> existingIds.add(t.getTransactionId()));
    
    // Only loads transactions NOT already in memory
    // Reports skipped count separately from loaded count
}
```

---

## Issue 2: Transaction Type Selection After Minimum Balance Warning

### Problem
When a user made a transaction that would violate the minimum balance requirements and selected option 2 ("Choose a different transaction type"), they were taken back to the beginning and had to re-enter their account number.

### Root Cause
The code was calling `processTransactionMain()` recursively which restarts the entire flow including account number entry:
```java
if (option == 2) {
    return processTransactionMain();  // <-- This restarts from beginning
}
```

### Solution Applied

#### `TransactionProcessingInput.java` Changes:
Created a new method `processTransactionForAccount()` that handles transaction processing for an already-validated account:

```java
/**
 * Process transaction for a specific account (skips account number entry).
 * Called when user selects option 2 to do another transaction type.
 */
public static TransactionRequest processTransactionForAccount(Account senderAccount, String senderAccountNumber) {
    // Displays account details
    // Shows transaction type menu (Deposit/Withdrawal/Transfer)
    // Handles amount entry and validation
    // Returns TransactionRequest without asking for account number again
}

// Updated processTransactionMain() to call the new method:
public static TransactionRequest processTransactionMain() {
    // Get account number from user
    // Validate account exists
    return processTransactionForAccount(senderAccount, senderAccountNumber);
}

// Updated the warning handlers:
if (option == 2) {
    return processTransactionForAccount(senderAccount, senderAccountNumber);  // Fixed!
}
```

---

## Issue 3: Transaction Data Deletion During Program Execution

### Problem
Transaction data was being deleted/lost while the program was running.

### Root Causes
1. **Account Loading Bug**: When loading accounts from file, the system was creating NEW accounts with NEW account numbers instead of preserving the original account numbers. This caused:
   - `accountCounter` to increment unnecessarily
   - Initial deposit transactions to be created for already-existing accounts
   - Transaction/Account number mismatch

2. **Missing "From File" Constructors**: The account creation path for file-loaded accounts was the same as new account creation, triggering transaction creation.

3. **NewTransactions Tracking Issue**: Transactions created during account loading were being tracked as "new" and could cause data corruption on save.

### Solutions Applied

#### New Constructors for File Loading

**`Account.java`:**
```java
/**
 * Constructor for loading account from file.
 * Preserves the original account number and updates the counter if needed.
 */
public Account(Customer customer, String accountNumber, boolean fromFile) {
    this.customer = customer;
    this.accountNumber = accountNumber;
    // Update counter to prevent duplicate numbers for new accounts
    if (accountNumber.startsWith("ACC")) {
        int num = Integer.parseInt(accountNumber.substring(3));
        if (num > accountCounter) {
            accountCounter = num;
        }
    }
}
```

**`SavingsAccount.java`:**
```java
/**
 * Constructor for loading account from file.
 * Does NOT create an initial deposit transaction.
 */
public SavingsAccount(Customer customer, double balance, String accountNumber, boolean fromFile) {
    super(customer, accountNumber, fromFile);
    this.setStatus("Active");
    super.updateBalance(balance);
    // No transaction is created
}
```

**`CheckingAccount.java`:**
```java
/**
 * Constructor for loading account from file.
 * Does NOT create an initial deposit transaction.
 */
public CheckingAccount(Customer customer, double balance, String accountNumber, boolean fromFile) {
    super(customer, accountNumber, fromFile);
    this.setStatus("Active");
    super.updateBalance(balance);
    // No transaction is created
}
```

**`Customer.java`:**
```java
/**
 * Constructor for loading customer from file.
 * Preserves the original customer ID.
 */
public Customer(String name, int age, String contact, String address, String customerId, boolean fromFile) {
    // Sets fields directly without incrementing customerCounter
    // Updates counter if needed to prevent duplicates
}
```

**`RegularCustomer.java` and `PremiumCustomer.java`:**
```java
public RegularCustomer(String name, int age, String contact, String address, String customerId, boolean fromFile) {
    super(name, age, contact, address, customerId, fromFile);
}

public PremiumCustomer(String name, int age, String contact, String address, String customerId, boolean fromFile) {
    super(name, age, contact, address, customerId, fromFile);
}
```

#### Updated File Parsing

**`FileIOUtils.java` - `parseAccount()` method:**
```java
public static void parseAccount(String line) {
    // Parse all fields from the line
    String accountNumber = columns[0].trim();
    String customerId = columns[5].trim();
    // ...
    
    // Create customer using file-loading constructor (preserves ID)
    Customer customer;
    if ("Regular".equalsIgnoreCase(customerType)) {
        customer = new RegularCustomer(name, age, contact, address, customerId, true);
    } else {
        customer = new PremiumCustomer(name, age, contact, address, customerId, true);
    }
    
    // Create account using file-loading constructor (preserves number, no transaction)
    Account account;
    if ("Savings".equalsIgnoreCase(accountType)) {
        account = new SavingsAccount(customer, balance, accountNumber, true);
    } else {
        account = new CheckingAccount(customer, balance, accountNumber, true);
    }
    
    // Add without tracking as "newly created"
    accountManager.addAccountFromFile(account);
}
```

**`AccountManager.java` - New method:**
```java
/**
 * Adds an account loaded from file.
 * Does NOT track it as newly created (won't be re-saved).
 */
public boolean addAccountFromFile(Account account) {
    if (accounts.containsKey(account.getAccountNumber())) {
        return false;  // Skip duplicates silently
    }
    accounts.put(account.getAccountNumber(), account);
    accountCount.getAndIncrement();
    // Don't add to newlyCreatedAccountNumbers
    return true;
}
```

---

## Files Modified

| File | Changes |
|------|---------|
| `TransactionManager.java` | Added `dataLoaded` flag, `addLoadedTransaction()` method, fixed save/load logic |
| `TransactionProcessingInput.java` | Added `processTransactionForAccount()` method, fixed option 2 handling |
| `FileIOUtils.java` | Simplified `readTransactionsFromFile()`, updated `parseAccount()` to use new constructors |
| `Account.java` | Added file-loading constructor |
| `SavingsAccount.java` | Added file-loading constructor |
| `CheckingAccount.java` | Added file-loading constructor |
| `Customer.java` | Added file-loading constructor |
| `RegularCustomer.java` | Added file-loading constructor |
| `PremiumCustomer.java` | Added file-loading constructor |
| `AccountManager.java` | Added `addAccountFromFile()` method |

---

## Requirements Verification

### Feature 1: Collections Migration with Functional Programming ✅
- Uses `ArrayList` for transactions storage (`List<Transaction>`)
- Uses `HashMap<String, Account>` for account storage
- Uses `Streams` for filtering, mapping (see `TransactionManager.filterTransactionsByType()`, `FunctionalUtils`)
- Uses `Lambda expressions` throughout (see `ValidationUtils` predicates)
- Uses `Comparator` with streams for sorting

### Feature 2: File Persistence with Functional Stream Processing ✅
- Saves accounts and transactions to `accounts.txt` and `transactions.txt`
- Loads data automatically on startup via `AccountManager.loadAccountsOnStart()` and `TransactionManager.loadTransactionsOnStart()`
- Uses Java NIO `Files` and `Paths` APIs
- Uses `Streams` and method references for parsing (see `FileIOUtils.serializeTransaction()`)

### Feature 3: Regex Validation ✅
- Account number validation: `ACC\d{3}` pattern in `ValidationUtils`
- Email validation: Standard email regex pattern
- Phone number validation: Multiple format support
- Predicate lambdas for dynamic validation rules (see `ValidationUtils.isValidAccountNumber`, etc.)

### Feature 4: Thread-Safe Concurrent Transactions ✅
- Uses `Thread` and `synchronized` methods (see `Account.updateBalance()`)
- Uses `ExecutorService` in `ConcurrencyUtils.runConcurrentTransactions()`
- Uses `Collections.synchronizedList()` for thread-safe transaction list
- Uses `CopyOnWriteArrayList` for new transactions
- Prevents race conditions with synchronized blocks

### Feature 5: Enhanced Console Experience ✅
- Shows data load/save confirmation messages
- Displays thread activities in real time (in `ConcurrencyUtils`)
- Maintains readable logs for file operations

---

## Issue 4: Negative Transaction Amounts for Withdrawals

### Problem
- Withdrawal transactions were being stored with negative amounts (e.g., `-2.00` instead of `2.00`)
- This was inconsistent since the transaction type field ("Withdrawal", "Transfer Out") already indicates the direction

### Root Cause
In both `SavingsAccount` and `CheckingAccount`, the withdrawal methods were creating transactions with `-amount`:
```java
// OLD CODE - INCORRECT
newTransaction = new Transaction(
    this.getAccountNumber(),
    transactionType,
    -amount,  // <-- Negative amount was wrong
    this.getBalance()
);
```

### Solution Applied
Changed all withdrawal transaction creation to use positive amounts:

**`SavingsAccount.java` - `withdrawWithType()` method:**
```java
// NEW CODE - CORRECT
newTransaction = new Transaction(
    this.getAccountNumber(),
    transactionType,
    amount,  // <-- Now positive
    this.getBalance()
);
```

**`CheckingAccount.java` - `withdraw()` and `withdrawWithType()` methods:**
```java
// NEW CODE - CORRECT  
newTransaction = new Transaction(
    this.getAccountNumber(),
    transactionType,
    amount,  // <-- Now positive
    this.getBalance()
);
```

**`transactions.txt` - Corrected existing data:**
```
TXN003|ACC001|Withdrawal|2.00|598.00|06-01-2026 08:40:31 PM
```
(Changed from `-2.00` to `2.00`)

---

## Test Results

All 87 tests pass after the changes:
```
Tests run: 87, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## Usage Notes

1. **Data Directory**: Account and transaction data is stored in `src/main/java/com/miracle/data/`
2. **On Startup**: The system automatically loads `accounts.txt` and `transactions.txt`
3. **On Exit**: The system saves only NEW accounts and transactions (appends to files)
4. **Transaction Flow**: When a balance warning occurs and user chooses option 2, they now go directly to transaction type selection without re-entering account number
5. **Transaction Amounts**: All transaction amounts are now stored as positive values. The transaction type ("Deposit", "Withdrawal", "Transfer In", "Transfer Out") indicates the direction.

---

## Future Improvements Suggested

1. Consider using a database for persistence instead of text files
2. Add transaction rollback mechanism for failed multi-step operations
3. Implement audit logging for all account changes
4. Add encryption for sensitive data in files

