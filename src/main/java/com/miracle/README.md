# BankAccountManagement
Amalitech Java Bank Account Management System - Version 3

# 🏦 Bank Account Management System

## Project Overview

This is a console-based Java application designed to simulate a comprehensive bank
account management system. It provides core functionality for creating, viewing,
and managing customer accounts and their transactions, utilizing modern Java features including:

- **Object-Oriented Programming (OOP)** principles
- **Java Collections Framework** (ArrayList, HashMap, HashSet)
- **Functional Programming** (Lambdas, Streams, Method References)
- **File I/O with NIO** (Path API, Files API)
- **Regular Expressions** for input validation
- **Concurrency** with threads and synchronized methods

---

## ✨ Features

The application provides the following core functions via a simple command-line interface:

### Core Functionality
* **Create Account:** Register new customer accounts (Savings or Checking)
* **View Accounts:** Display a comprehensive list of all active accounts, including total bank balance
* **View Account Statement:** Generate detailed account statements
* **View All Customers:** Display all registered customers
* **View All Transactions:** View transaction history by the bank
* **Process Transaction:** Handle deposits, withdrawals, and transfers
* **View Transaction History:** Display chronological list of transactions for any account
* **Data Persistence:** Save and load accounts/transactions to/from files
* **Concurrent Simulations:** Test thread-safe transactions

### Account Types
| Type | Key Features |
|------|--------------|
| **Savings Account** | Earns interest (3.5% annually). Enforces a **minimum balance** of **$500** on withdrawals. |
| **Checking Account** | Does not earn interest. Has an **overdraft limit** of **$1,000**. Applies a **$10 monthly fee**. |

### Customer Types
| Type | Key Features |
|------|--------------|
| **Regular Customer** | Standard banking services. |
| **Premium Customer** | Enhanced benefits, including waived monthly fees on Checking Accounts. |

---

## 🗂️ Java Collections Usage

The system uses Java Collections Framework for efficient data management, replacing traditional arrays.

### Collections Implementation

| Collection Type | Location | Purpose |
|-----------------|----------|---------|
| `HashMap<String, Account>` | `AccountManager.java` | Store accounts with O(1) lookup by account number |
| `HashSet<String>` | `AccountManager.java` | Track newly created account numbers |
| `ArrayList<Transaction>` | `TransactionManager.java` | Store all transactions |
| `CopyOnWriteArrayList<Transaction>` | `TransactionManager.java` | Thread-safe list for new transactions |

### Code Examples

```java
// AccountManager.java - HashMap for accounts
private Map<String, Account> accounts = new HashMap<>();
private Set<String> newlyCreatedAccountNumbers = new HashSet<>();

// TransactionManager.java - Synchronized ArrayList
private static final List<Transaction> transactions = 
    Collections.synchronizedList(new ArrayList<>());
private static final List<Transaction> newTransactions = 
    new CopyOnWriteArrayList<>();
```

### Benefits Over Arrays
- **Dynamic sizing:** No fixed capacity limits
- **O(1) lookups:** HashMap provides constant-time account retrieval
- **Built-in methods:** `put()`, `get()`, `remove()`, `containsKey()`, etc.
- **Thread-safe options:** `Collections.synchronizedList()`, `CopyOnWriteArrayList`

---

## 🔄 Streams API & Functional Programming

The system extensively uses Java Streams API and functional programming constructs for data processing.

### Lambda Expressions

| Location | Usage | Code |
|----------|-------|------|
| `TransactionManager.java` | Filter by type | `t -> t.getType().toUpperCase().equals(normalizedType)` |
| `TransactionManager.java` | Filter by account | `t -> t.getAccountNumber().equalsIgnoreCase(accountNumber)` |
| `ValidationUtils.java` | Predicate validation | `input -> input != null && PATTERN.matcher(input).matches()` |

### Stream Operations

```java
// Filter transactions by type
public static List<Transaction> filterTransactionsByType(String type) {
    return transactions.stream()
            .filter(t -> t != null && t.getType() != null)
            .filter(t -> t.getType().toUpperCase().equals(normalizedType))
            .collect(Collectors.toList());
}

// Get transactions by account
public List<Transaction> getTransactionsByAccount(String accountNumber) {
    return transactions.stream()
            .filter(Objects::nonNull)
            .filter(t -> t.getAccountNumber().equalsIgnoreCase(accountNumber))
            .toList();
}

// Group transactions by type
public Map<String, List<Transaction>> groupTransactionsByType() {
    return transactions.stream()
            .collect(Collectors.groupingBy(Transaction::getType));
}

// Calculate total balance with parallel stream
public double getTotalBalance() {
    return accounts.values().stream()
            .parallel()
            .mapToDouble(Account::getBalance)
            .sum();
}
```

### Method References

| Type | Example | Location |
|------|---------|----------|
| Static method | `FileIOUtils::serializeTransaction` | `FileIOUtils.java` |
| Instance method | `Transaction::getType` | `TransactionManager.java` |
| Instance method | `Account::getBalance` | `AccountManager.java` |
| Static method | `Objects::nonNull` | `StatementGenerator.java` |

### Functional Interfaces (Predicates)

```java
// ValidationUtils.java - Predicate-based validation
public static final Predicate<String> isValidAccountNumber =
    input -> input != null && ACCOUNT_PATTERN.matcher(input).matches();

public static final Predicate<String> isValidEmail =
    input -> input != null && EMAIL_PATTERN.matcher(input).matches();

public static final Predicate<Double> isPositiveAmount =
    amount -> amount != null && amount > 0;

public static final Predicate<Integer> isValidAge =
    age -> age != null && age > 18 && age < 120;
```

### Sorting with Streams

```java
// FunctionalUtils.java - Sort by amount
public static List<Transaction> sortTransactionsByAmount(List<Transaction> transactions) {
    return transactions.stream()
            .sorted(Comparator.comparing(Transaction::getAmount))
            .collect(Collectors.toList());
}

// Sort by date (descending)
public static List<Transaction> sortTransactionsByDate(List<Transaction> transactions) {
    return transactions.stream()
            .sorted(Comparator.comparing(Transaction::getTimestamp).reversed())
            .collect(Collectors.toList());
}
```

---

## 🔐 Concurrency & Thread Safety

The system implements thread-safe operations to handle concurrent transactions without data corruption.

### Synchronized Methods

All account balance-modifying methods are synchronized to prevent race conditions:

```java
// Account.java - Thread-safe balance update
public synchronized double updateBalance(double newBalance) {
    this.balance = newBalance;
    return this.balance;
}

// SavingsAccount.java - Synchronized deposit
@Override
public synchronized Transaction deposit(double amount) throws InvalidAmountException {
    return depositWithType(amount, "Deposit");
}

// SavingsAccount.java - Synchronized withdrawal
@Override
public synchronized Transaction withdraw(double amount) 
        throws InvalidAmountException, InsufficientFundsException {
    // ... validation and withdrawal logic
}

// CheckingAccount.java - Synchronized methods
@Override
public synchronized Transaction deposit(double amount) throws InvalidAmountException { ... }

@Override
public synchronized Transaction withdraw(double amount) 
        throws InvalidAmountException, OverdraftExceededException { ... }
```

### Thread-Safe Collections

```java
// TransactionManager.java
// Synchronized list for all transactions
private static final List<Transaction> transactions = 
    Collections.synchronizedList(new ArrayList<>());

// CopyOnWriteArrayList for concurrent iteration safety
private static final List<Transaction> newTransactions = 
    new CopyOnWriteArrayList<>();
```

### Volatile Fields

```java
// Account.java - Volatile for visibility across threads
private volatile double balance;

// TransactionManager.java - Volatile flag
private static volatile boolean dataLoaded = false;
```

### Concurrent Transaction Simulation

```java
// ConcurrencyUtils.java - Multi-threaded transaction test
public static void runConcurrentTransactions(Account account) {
    ExecutorService executor = Executors.newFixedThreadPool(TRANSACTION_COUNT);
    AtomicInteger successfulOps = new AtomicInteger();
    
    for (int i = 0; i < TRANSACTION_COUNT; i++) {
        final int threadNum = i + 1;
        executor.submit(() -> {
            try {
                // Deposit
                System.out.println("Thread-" + threadNum + ": Depositing $100");
                account.deposit(100.0);
                
                // Withdraw
                System.out.println("Thread-" + threadNum + ": Withdrawing $100");
                account.withdraw(100.0);
                
                successfulOps.incrementAndGet();
            } catch (Exception e) {
                System.err.println("Thread-" + threadNum + " failed: " + e.getMessage());
            }
        });
    }
    
    executor.shutdown();
    executor.awaitTermination(60, TimeUnit.SECONDS);
    
    System.out.println("√ Thread-safe operations completed successfully.");
    System.out.printf("Final Balance: $%,.2f%n", account.getBalance());
}
```

### Parallel Streams

```java
// AccountManager.java - Parallel processing for performance
public double getTotalBalance() {
    return accounts.values().stream()
            .parallel()
            .mapToDouble(Account::getBalance)
            .sum();
}
```

### Thread Safety Summary

| Mechanism | Location | Purpose |
|-----------|----------|---------|
| `synchronized` methods | Account classes | Prevent concurrent balance modifications |
| `volatile` fields | Account, TransactionManager | Ensure visibility across threads |
| `Collections.synchronizedList()` | TransactionManager | Thread-safe list operations |
| `CopyOnWriteArrayList` | TransactionManager | Safe iteration during modifications |
| `AtomicInteger` | ConcurrencyUtils, AccountManager | Thread-safe counters |
| `ExecutorService` | ConcurrencyUtils | Managed thread pool |

---

## 📁 File I/O with NIO

The system uses Java NIO for persistent storage of accounts and transactions.

### Path API Usage

```java
// FileIOUtils.java
private static final String DATA_DIR = "src/main/java/com/miracle/data";
private static final Path accountFile = Paths.get(DATA_DIR, "accounts.txt");
private static final Path transactionFile = Paths.get(DATA_DIR, "transactions.txt");
```

### Files API Operations

```java
// Read with streams
List<String> lines = Files.readAllLines(accountFile).stream()
        .filter(line -> !line.trim().isEmpty())
        .toList();

// Write with options
Files.write(transactionFile, lines, 
    StandardOpenOption.CREATE, 
    StandardOpenOption.APPEND);

// Check existence
if (Files.notExists(transactionFile)) { ... }

// Create directories
Files.createDirectories(dir);
```

---

## 🚀 Getting Started

### Prerequisites
* Java Development Kit (JDK) 21 or newer
* Maven 3.8+
* IntelliJ IDEA (recommended) or any Java IDE

### Setup and Running

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd BankAccountManagementV3
   ```

2. **Compile the project:**
   ```bash
   mvn compile
   ```

3. **Run tests:**
   ```bash
   mvn test
   ```

4. **Run the application:**
   ```bash
   mvn exec:java -Dexec.mainClass="com.miracle.Main"
   ```

The application will load existing data from files and present the main menu for interaction.

---

## 🌱 Data Seeding

The system includes a `DataSeeder` utility for populating initial demo data.

### Seeding Initial Data

From the main menu, navigate to:
```
4. Save/Load Data → 3. Seed Initial Data (Demo)
```

### What Gets Seeded

**5 Sample Accounts:**
| Account | Customer | Type | Initial Balance |
|---------|----------|------|-----------------|
| ACC001 | John Mensah | Regular - Savings | $5,000.00 |
| ACC002 | Ama Serwaa | Regular - Checking | $2,500.00 |
| ACC003 | Kwame Asante | Premium - Savings | $15,000.00 |
| ACC004 | Efua Owusu | Premium - Checking | $25,000.00 |
| ACC005 | Kofi Adjei | Regular - Savings | $1,000.00 |

**Sample Transactions:**
- Deposits and withdrawals for each account
- Transfer between accounts (ACC004 → ACC001)

### Usage in Code

```java
// Seed data if no existing data
DataSeeder.seedInitialData();

// Force re-seed (for testing)
DataSeeder.forceReseed();

// Check if data has been seeded
boolean seeded = DataSeeder.isDataSeeded();

// Display seed summary
DataSeeder.displaySeedSummary();
```

### Behavior
- **Skips seeding** if accounts already exist (loaded from file)
- **Prevents duplicates** by tracking seed status
- **Creates transactions** as part of the seeding process

---

## 📊 Test Coverage

| Test Class | Tests | Description |
|------------|-------|-------------|
| `AccountTest.java` | 29 | Account creation, deposits, withdrawals |
| `ExceptionTest.java` | 38 | Exception handling |
| `TransactionManagerTest.java` | 20 | Transaction management |
| `FileIOTest.java` | 22 | File I/O operations, DataSeeder |
| **Total** | **109** | All tests passing ✅ |

---

## 📝 License

This project is part of the Amalitech Java Training Program.
