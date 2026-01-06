# Bank Account Management System - Requirements Implementation Guide

**Project:** Bank Account Management System V3  
**Date:** January 6, 2026  
**Version:** 3.1.2  
**Technology Stack:** Java 21 (LTS), IntelliJ IDEA Community Edition

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Feature 1: Collections Migration with Functional Programming](#feature-1-collections-migration-with-functional-programming)
3. [Feature 2: File Persistence with Functional Stream Processing](#feature-2-file-persistence-with-functional-stream-processing)
4. [Feature 3: Regex Validation](#feature-3-regex-validation)
5. [Feature 4: Thread-Safe Concurrent Transactions](#feature-4-thread-safe-concurrent-transactions)
6. [Feature 5: Enhanced Console Experience](#feature-5-enhanced-console-experience)
7. [Project Structure](#project-structure)
8. [Test Coverage](#test-coverage)
9. [Learning Objectives Achievement](#learning-objectives-achievement)

---

## Project Overview

This Bank Account Management System demonstrates modern Java features including:
- ✅ Migration from arrays to Java Collections
- ✅ Functional Programming (Lambdas, Functional Interfaces, Method References, Streams API)
- ✅ File I/O and NIO (Path API) for persistent storage
- ✅ Regular Expressions (Regex) for input validation
- ✅ Concurrency Fundamentals with threads and synchronized methods

---

## Feature 1: Collections Migration with Functional Programming

### Requirement
> Replace arrays with ArrayList and HashMap<String, Account>. Implement efficient search, insert, and update operations. Support sorting transactions by date or amount using comparators and Streams. Use Lambda expressions for concise iteration and transformation of data.

### Implementation

#### ArrayList Usage

| File | Location | Code Example |
|------|----------|--------------|
| `TransactionManager.java` | Line 14 | `private static final List<Transaction> transactions = Collections.synchronizedList(new ArrayList<>());` |
| `TransactionManager.java` | Line 15 | `private static final List<Transaction> newTransactions = new CopyOnWriteArrayList<>();` |
| `FileIOUtils.java` | Line 142 | `List<Transaction> loadedTransactions = new ArrayList<>();` |

#### HashMap Usage

| File | Location | Code Example |
|------|----------|--------------|
| `AccountManager.java` | Line 27 | `private Map<String, Account> accounts = new HashMap<>();` |
| `AccountManager.java` | Line 29 | `private Set<String> newlyCreatedAccountNumbers = new HashSet<>();` |

#### Stream Operations and Lambdas

| File | Method | Description | Code |
|------|--------|-------------|------|
| `TransactionManager.java` | `filterTransactionsByType()` | Filters transactions by type using streams | `transactions.stream().filter(t -> t.getType().toUpperCase().equals(normalizedType)).collect(Collectors.toList())` |
| `TransactionManager.java` | `getTransactionsByAccount()` | Gets transactions for specific account | `transactions.stream().filter(t -> t.getAccountNumber().equalsIgnoreCase(accountNumber)).toList()` |
| `TransactionManager.java` | `groupTransactionsByType()` | Groups transactions by type | `transactions.stream().collect(Collectors.groupingBy(Transaction::getType))` |
| `AccountManager.java` | `getTotalBalance()` | Calculates total balance with parallel stream | `accounts.values().stream().parallel().mapToDouble(Account::getBalance).sum()` |
| `FunctionalUtils.java` | `sortTransactionsByAmount()` | Sorts using Comparator | `transactions.stream().sorted(Comparator.comparing(Transaction::getAmount)).collect(Collectors.toList())` |
| `FunctionalUtils.java` | `sortTransactionsByDate()` | Sorts by timestamp descending | `transactions.stream().sorted(Comparator.comparing(Transaction::getTimestamp).reversed()).collect(Collectors.toList())` |

#### Method References

| File | Location | Usage |
|------|----------|-------|
| `TransactionManager.java` | `groupTransactionsByType()` | `Transaction::getType` |
| `AccountManager.java` | `getTotalBalance()` | `Account::getBalance` |
| `FileIOUtils.java` | `saveTransactionsToFile()` | `FileIOUtils::serializeTransaction` |
| `StatementGenerator.java` | `displayAllTransactions()` | `Objects::nonNull` |

---

## Feature 2: File Persistence with Functional Stream Processing

### Requirement
> Save all accounts and transactions to files (accounts.txt, transactions.txt). Load data automatically on startup. Use Java NIO Files and Paths APIs for reading/writing. Process loaded data using Streams and Method References.

### Implementation

#### Data Files

| File | Path | Format |
|------|------|--------|
| `accounts.txt` | `src/main/java/com/miracle/data/accounts.txt` | `ACC001\|Name\|Age\|Contact\|Address\|CUS001\|CustomerType\|AccountType\|Balance` |
| `transactions.txt` | `src/main/java/com/miracle/data/transactions.txt` | `TXN001\|ACC001\|Type\|Amount\|BalanceAfter\|Timestamp` |

#### NIO Path API Usage

| File | Location | Code |
|------|----------|------|
| `FileIOUtils.java` | Lines 24-35 | Path definitions using `Paths.get()` |

```java
private static final String DATA_DIR = "src/main/java/com/miracle/data";
private static final Path accountFile = Paths.get(DATA_DIR, ACCOUNTS_FILE_NAME);
private static final Path transactionFile = Paths.get(DATA_DIR, TRANSACTIONS_FILE_NAME);
```

#### NIO Files API Usage

| Operation | File | Method | Code |
|-----------|------|--------|------|
| **Read** | `FileIOUtils.java` | `readAccountsFromFile()` | `Files.readAllLines(accountFile)` |
| **Read** | `FileIOUtils.java` | `readTransactionsFromFile()` | `Files.readAllLines(transactionFile)` |
| **Write** | `FileIOUtils.java` | `saveAccountsToFile()` | `Files.write(accountFile, lines, StandardOpenOption.CREATE, StandardOpenOption.APPEND)` |
| **Write** | `FileIOUtils.java` | `saveTransactionsToFile()` | `Files.write(transactionFile, lines, StandardOpenOption.CREATE, StandardOpenOption.APPEND)` |
| **Check Existence** | `FileIOUtils.java` | Multiple | `Files.notExists(transactionFile)` |
| **Create Directories** | `FileIOUtils.java` | `ensureDataDirExists()` | `Files.createDirectories(dir)` |

#### Stream Processing for File I/O

| File | Method | Stream Usage |
|------|--------|--------------|
| `FileIOUtils.java` | `readAccountsFromFile()` | `Files.readAllLines(accountFile).stream().filter(line -> !line.trim().isEmpty()).toList()` |
| `FileIOUtils.java` | `readTransactionsFromFile()` | `Files.readAllLines(transactionFile).stream().filter(line -> !line.trim().isEmpty()).toList()` |
| `FileIOUtils.java` | `saveTransactionsToFile()` | `txns.stream().map(FileIOUtils::serializeTransaction).collect(Collectors.toList())` |
| `FileIOUtils.java` | `serializeAccounts()` | `accounts.entrySet().stream().map(entry -> {...}).toList()` |

#### Automatic Loading on Startup

| File | Method | Called From |
|------|--------|-------------|
| `AccountManager.java` | `loadAccountsOnStart()` | `Main.main()` |
| `TransactionManager.java` | `loadTransactionsOnStart()` | `Main.main()` |

```java
// Main.java - Lines 39-41
System.out.println("\n Loading account data from files...\n");
AccountManager.loadAccountsOnStart();
TransactionManager.loadTransactionsOnStart();
```

---

## Feature 3: Regex Validation

### Requirement
> Validate account numbers (ACC\d{3}), emails, and phone numbers. Display user-friendly errors for invalid input formats. Centralize validation logic in ValidationUtils. Optionally, apply Predicate lambdas for dynamic validation rules.

### Implementation

#### Regex Patterns

| Pattern | File | Location | Regex |
|---------|------|----------|-------|
| Account Number | `ValidationUtils.java` | Line 13 | `^ACC\\d{3}$` |
| Email | `ValidationUtils.java` | Line 16-18 | `^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$` |
| Phone Number | `ValidationUtils.java` | Line 21-23 | `^(\\+\\d{1,3})?[-.\\s]?\\(?\\d{1,4}\\)?[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,9}$` |

#### Predicate-based Validation (Functional Interfaces)

| Predicate | File | Location | Usage |
|-----------|------|----------|-------|
| `isValidAccountNumber` | `ValidationUtils.java` | Line 28-29 | Validates ACC followed by 3 digits |
| `isValidEmail` | `ValidationUtils.java` | Line 34-35 | Validates email format |
| `isValidPhoneNumber` | `ValidationUtils.java` | Line 40-41 | Validates phone number format |
| `isNotEmpty` | `ValidationUtils.java` | Line 46-47 | Validates non-empty strings |
| `isPositiveAmount` | `ValidationUtils.java` | Line 52-53 | Validates positive amounts |
| `isValidAge` | `ValidationUtils.java` | Line 58-59 | Validates age (18-120) |

```java
// Example Predicate definitions
public static final Predicate<String> isValidAccountNumber =
    input -> input != null && ACCOUNT_PATTERN.matcher(input).matches();

public static final Predicate<String> isValidEmail =
    input -> input != null && EMAIL_PATTERN.matcher(input).matches();
```

#### Validation Methods

| Method | File | Purpose |
|--------|------|---------|
| `getValidatedInput()` | `ValidationUtils.java` | Generic regex validation with retry |
| `getValidAgeInput()` | `ValidationUtils.java` | Age validation (18-120) |
| `getValidAccountNumber()` | `ValidationUtils.java` | Account number validation with format check |
| `getValidAmount()` | `ValidationUtils.java` | Amount validation (positive numbers) |

#### User-Friendly Error Messages

```java
// ValidationUtils.java - getValidAccountNumber()
System.out.println("Invalid account format. Account number must be ACC followed by three digits.");

// ValidationUtils.java - getValidatedInput()
System.out.println("Error: " + errorMessage);
```

---

## Feature 4: Thread-Safe Concurrent Transactions

### Requirement
> Use Thread and synchronized to simulate multiple deposits/withdrawals. Demonstrate concurrency by running simultaneous operations safely. Prevent race conditions and data inconsistencies. Optionally, use parallel streams for batch transaction simulations.

### Implementation

#### Synchronized Methods

| File | Method | Keyword |
|------|--------|---------|
| `Account.java` | `updateBalance()` | `synchronized` |
| `SavingsAccount.java` | `deposit()` | `synchronized` |
| `SavingsAccount.java` | `withdraw()` | `synchronized` |
| `SavingsAccount.java` | `depositWithType()` | `synchronized` |
| `SavingsAccount.java` | `withdrawWithType()` | `synchronized` |
| `CheckingAccount.java` | `deposit()` | `synchronized` |
| `CheckingAccount.java` | `withdraw()` | `synchronized` |
| `CheckingAccount.java` | `depositWithType()` | `synchronized` |
| `CheckingAccount.java` | `withdrawWithType()` | `synchronized` |

```java
// Account.java - Line 89
public synchronized double updateBalance(double newBalance) {
    this.balance = newBalance;
    return this.balance;
}

// SavingsAccount.java - Line 73
@Override
public synchronized Transaction deposit(double amount) throws InvalidAmountException {
    return depositWithType(amount, "Deposit");
}
```

#### Thread-Safe Collections

| File | Location | Collection |
|------|----------|------------|
| `TransactionManager.java` | Line 14 | `Collections.synchronizedList(new ArrayList<>())` |
| `TransactionManager.java` | Line 15 | `CopyOnWriteArrayList<>()` |
| `Account.java` | Line 15 | `private volatile double balance` |

#### Concurrent Transaction Simulation

| File | Class | Features |
|------|-------|----------|
| `ConcurrencyUtils.java` | `ConcurrencyUtils` | ExecutorService, thread pool, concurrent operations |

```java
// ConcurrencyUtils.java - Key implementation
public static void runConcurrentTransactions(Account account) {
    ExecutorService executor = Executors.newFixedThreadPool(TRANSACTION_COUNT);
    AtomicInteger successfulOps = new AtomicInteger();
    
    for (int i = 0; i < TRANSACTION_COUNT; i++) {
        executor.submit(() -> {
            account.deposit(transactionAmount);
            account.withdraw(transactionAmount);
            successfulOps.incrementAndGet();
        });
    }
    // Wait for completion...
}
```

#### Parallel Streams

| File | Method | Usage |
|------|--------|-------|
| `AccountManager.java` | `getTotalBalance()` | `.stream().parallel().mapToDouble(Account::getBalance).sum()` |

---

## Feature 5: Enhanced Console Experience

### Requirement
> Show data load/save confirmation messages. Display thread activities in real time. Maintain readable logs for file operations and thread actions.

### Implementation

#### Data Load/Save Confirmation Messages

| Operation | File | Message Example |
|-----------|------|-----------------|
| Load Accounts | `FileIOUtils.java` | `"X accounts loaded successfully from accounts.txt."` |
| Load Transactions | `FileIOUtils.java` | `"X new transactions loaded from transactions.txt"` |
| Load Transactions (Skipped) | `FileIOUtils.java` | `"X transactions already in memory (skipped)"` |
| Save Accounts | `FileIOUtils.java` | `"Successfully saved X new account(s)."` |
| Save Transactions | `FileIOUtils.java` | `"Successfully saved X new transaction(s)."` |
| No Data to Save | `TransactionManager.java` | `"No new transactions to save."` |
| Load Complete | `TransactionManager.java` | `"Transaction data loaded. Total transactions in memory: X"` |

#### Thread Activity Display

| File | Feature | Example Output |
|------|---------|----------------|
| `ConcurrencyUtils.java` | Thread identification | `"Thread-X: Depositing $100 to ACC001"` |
| `ConcurrencyUtils.java` | Thread identification | `"Thread-X: Withdrawing $100 from ACC001"` |
| `ConcurrencyUtils.java` | Completion status | `"√ Thread-safe operations completed successfully."` |
| `ConcurrencyUtils.java` | Final balance | `"Final Balance for ACC001: $1,000.00"` |

#### File Operation Logs

```java
// FileIOUtils.java - Various log messages
System.out.println("No transaction file found at: " + transactionFile);
System.err.println("Failed to load accounts from file: " + e.getMessage());
System.err.println("Skipping invalid transaction: " + line);
```

---

## Project Structure

```
src/
├── main/java/com/miracle/
│   ├── Main.java                           # Application entry point
│   ├── data/
│   │   ├── accounts.txt                    # Account data storage
│   │   └── transactions.txt                # Transaction data storage
│   ├── docs/
│   │   ├── collections-architecture.md     # Architecture documentation
│   │   ├── FIXES_AND_CHANGES.md           # Bug fixes documentation
│   │   └── REQUIREMENTS_IMPLEMENTATION.md  # This file
│   └── src/
│       ├── dto/
│       │   ├── AccountRequest.java         # Account creation DTO
│       │   └── TransactionRequest.java     # Transaction DTO
│       ├── handlers/
│       │   ├── AccountCreationHandler.java # Account creation logic
│       │   └── TransactionHandler.java     # Transaction processing logic
│       ├── models/
│       │   ├── Account.java                # Abstract account class
│       │   ├── SavingsAccount.java         # Savings account implementation
│       │   ├── CheckingAccount.java        # Checking account implementation
│       │   ├── Customer.java               # Abstract customer class
│       │   ├── RegularCustomer.java        # Regular customer type
│       │   ├── PremiumCustomer.java        # Premium customer type
│       │   ├── Transaction.java            # Transaction model
│       │   ├── Transactable.java           # Transaction interface
│       │   └── exceptions/
│       │       ├── AccountNotFoundException.java
│       │       ├── InsufficientFundsException.java
│       │       ├── InvalidAmountException.java
│       │       ├── OverdraftExceededException.java
│       │       └── TransactionFailedException.java
│       ├── services/
│       │   ├── AccountManager.java         # Account management (Singleton)
│       │   ├── TransactionManager.java     # Transaction management (Singleton)
│       │   ├── FilePersistenceService.java # File persistence orchestration
│       │   └── StatementGenerator.java     # Report generation
│       └── utils/
│           ├── ValidationUtils.java        # Regex validation utilities
│           ├── ConcurrencyUtils.java       # Thread-safe utilities
│           ├── FunctionalUtils.java        # Functional programming utilities
│           ├── FileIOUtils.java            # File I/O operations
│           ├── InputUtils.java             # Console input utilities
│           └── TransactionProcessingInput.java # Transaction input handling
└── test/java/com/miracle/runner/
    ├── AccountTest.java                    # Account tests (29 tests)
    ├── ExceptionTest.java                  # Exception tests (38 tests)
    ├── TransactionManagerTest.java         # Transaction tests (20 tests)
    └── FileIOTest.java                     # File I/O tests (20 tests)
```

---

## Test Coverage

### Test Summary

| Test Class | Tests | Description |
|------------|-------|-------------|
| `AccountTest.java` | 29 | Account creation, deposits, withdrawals, transfers |
| `ExceptionTest.java` | 38 | Exception handling for all custom exceptions |
| `TransactionManagerTest.java` | 20 | Transaction management, filtering, sorting |
| `FileIOTest.java` | 20 | File read/write, serialization, deserialization |
| **Total** | **107** | All tests passing ✅ |

### File I/O Test Categories

| Category | Tests | Description |
|----------|-------|-------------|
| Serialization | 4 | Transaction serialization/deserialization |
| Account Serialization | 2 | Account serialization |
| File Write | 3 | Save transactions to file |
| File Read | 2 | Read transactions/accounts from file |
| Manager Integration | 4 | TransactionManager/AccountManager file operations |
| Duplicate Prevention | 1 | Prevent duplicate loading |
| Data Integrity | 2 | Positive amounts, type preservation |
| Edge Cases | 2 | Large amounts, decimal precision |

---

## Learning Objectives Achievement

| Learning Objective | Status | Evidence |
|--------------------|--------|----------|
| Use ArrayList and HashMap for efficient account and transaction storage | ✅ | `AccountManager.java`, `TransactionManager.java` |
| Apply Lambdas, Streams, and Functional Interfaces to perform filtering, mapping, and reduction | ✅ | `FunctionalUtils.java`, `ValidationUtils.java`, `TransactionManager.java` |
| Apply File I/O (NIO Paths & Files) for saving and loading account data persistently | ✅ | `FileIOUtils.java` |
| Implement Regex-based validation for customer info and account formats | ✅ | `ValidationUtils.java` |
| Demonstrate basic concurrency with threads and synchronized methods | ✅ | `ConcurrencyUtils.java`, `Account.java`, `SavingsAccount.java`, `CheckingAccount.java` |
| Maintain thread safety and avoid data corruption during concurrent transactions | ✅ | `synchronized` methods, `volatile` fields, thread-safe collections |
| Design code that's modular, reusable, and ready for future database integration | ✅ | Singleton pattern, service layer, DTO pattern |

---

## Implementation Phases Completed

| Phase | Description | Status |
|-------|-------------|--------|
| Phase 1 | Collections & Functional Migration | ✅ Complete |
| Phase 2 | File Persistence | ✅ Complete |
| Phase 3 | Regex Validation | ✅ Complete |
| Phase 4 | Concurrency Integration | ✅ Complete |

---

## Running the Application

```bash
# Compile the project
mvn compile

# Run tests
mvn test

# Run the application
mvn exec:java -Dexec.mainClass="com.miracle.Main"
```

---

## Summary

All requirements have been successfully implemented:

1. **Collections Migration**: ✅ Arrays replaced with ArrayList and HashMap
2. **Functional Programming**: ✅ Lambdas, Streams, Method References, Functional Interfaces
3. **File Persistence**: ✅ NIO Path/Files API with stream processing
4. **Regex Validation**: ✅ Centralized in ValidationUtils with Predicate lambdas
5. **Concurrency**: ✅ Thread-safe transactions with synchronized methods
6. **Enhanced Console**: ✅ Load/save confirmations, thread activity logs

**Total Test Coverage: 107 tests - All Passing ✅**

