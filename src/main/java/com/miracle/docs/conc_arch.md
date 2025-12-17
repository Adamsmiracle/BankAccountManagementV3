### Summary of concurrency and thread‑safety changes

#### 1) Account-level synchronization primitives
- File: `src/main/java/com/miracle/src/models/Account.java`
    - Added `private volatile double balance;` so readers see the latest value across threads.
    - Added `private final Object balanceLock = new Object();` and a protected accessor `getBalanceLock()` so subclasses can synchronize critical balance updates.

Why: `volatile` ensures visibility; the per‑account lock provides atomic read‑modify‑write for deposits/withdrawals without a global lock.

#### 2) SavingsAccount operations made atomic
- File: `src/main/java/com/miracle/src/models/SavingsAccount.java`
    - Wrapped balance mutations in `synchronized (getBalanceLock()) { ... }` for:
        - `depositWithType(...)` (and `deposit(...)` which delegates)
        - `withdrawWithType(...)` (and `withdraw(...)` which delegates)
    - Pattern: compute/validate, then set balance and construct the `Transaction` within the lock; call `TransactionManager.addTransaction(...)` outside the lock to reduce contention.
    - Enforced minimum balance check inside the synchronized block to avoid race conditions when multiple withdrawals compete.

Effect: Concurrent deposits/withdrawals on the same savings account produce consistent balances and correctly recorded transactions.

#### 3) CheckingAccount operations made atomic
- File: `src/main/java/com/miracle/src/models/CheckingAccount.java`
    - Wrapped balance mutations in `synchronized (getBalanceLock()) { ... }` for:
        - `deposit(...)`
        - `withdraw(...)`
        - `depositWithType(...)`
        - `withdrawWithType(...)`
    - Performed overdraft validations and final balance updates inside the critical section.
    - Constructed the `Transaction` inside the lock; added it to the manager after releasing the lock.
    - Note: In `withdrawWithType(...)`, overdraft limit can be adjusted for premium customers before applying the update, still under synchronization.

Effect: Overdraft rules are enforced correctly under contention; balances remain consistent.

#### 4) Thread‑safe transaction storage and lifecycle
- File: `src/main/java/com/miracle/src/services/TransactionManager.java`
    - Replaced `ArrayList` with `CopyOnWriteArrayList` for:
        - `transactions`
        - `newTransactions`
    - Introduced internal `txLock` for atomic multi‑list operations and in‑place list mutations:
        - `addTransaction(...)` appends to both lists inside a synchronized block.
        - `sortTransactionsByAmount()` / `sortTransactionsByDate()` take a snapshot, sort it, then replace contents inside a synchronized block.
        - `clearTransactions()` clears both lists under the lock.
        - `saveTransactionsOnExit()` snapshots `newTransactions` inside the lock, clears it, then writes to disk without holding the lock.
    - `loadTransactionsOnStart()` repopulates `transactions` and resets `newTransactions` safely.

Effect: Transaction recording and retrieval are safe when multiple threads log transactions concurrently; sorting/persistence are performed deterministically without concurrent modification issues.

#### 5) Concurrency simulation utility
- File: `src/main/java/com/miracle/src/utils/ConcurrencyUtils.java`
    - Added `runConcurrentTransactions(Account account, int threadCount, int opsPerThread, double minAmount, double maxAmount, boolean depositsOnly)`:
        - Spawns N worker threads and uses a pair of `CountDownLatch` gates to start them simultaneously and join on completion.
        - Each worker performs random deposits/withdrawals and counts successes/failures.
        - Returns a `record Result(String accountNumber, double finalBalance, int successCount, int failureCount)` for quick verification.

Effect: Provides a reproducible way to stress‑test the thread‑safety of account operations and transaction logging.

### Design notes
- Per‑account locking keeps critical sections small and avoids global bottlenecks.
- Constructing `Transaction` inside the lock captures the exact post‑update balance; adding it to the shared manager outside the lock reduces contention on account locks.
- `volatile` balance helps read‑only paths (e.g., summaries) see up‑to‑date values even when not synchronized, while all mutations remain synchronized.

### Quick verification snippet
```java
var acct = new com.miracle.src.models.SavingsAccount(
    new com.miracle.src.models.RegularCustomer("CUST001", "John Doe", 30, "0241234567", "Accra"),
    1000.00
);
var result = com.miracle.src.utils.ConcurrencyUtils.runConcurrentTransactions(
    acct,
    8,
    200,
    1.00, 50.00,
    false
);
System.out.printf("Account %s final balance: $%.2f (ok=%d, failed=%d)%n",
    result.accountNumber(), result.finalBalance(), result.successCount(), result.failureCount());
```

### Potential follow‑ups (optional)
- If accounts are created concurrently, consider synchronizing `Account.accountCounter` or generating IDs via `AtomicInteger` to avoid duplicates.
- Consider batching transaction persistence to reduce I/O overhead under very high throughput.
