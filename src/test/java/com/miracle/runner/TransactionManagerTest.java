package com.miracle.runner;

import com.miracle.src.models.Customer;
import com.miracle.src.models.exceptions.InsufficientFundsException;
import com.miracle.src.models.exceptions.OverdraftExceededException;
import com.miracle.src.services.TransactionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import com.miracle.src.models.*;
import com.miracle.src.models.exceptions.InvalidAmountException;

// removed reflection usage; no imports needed

public class TransactionManagerTest {

    private TransactionManager transactionManager;
    private Customer testCustomer;
    private Account testAccount;

    @BeforeEach
    public void setUp() throws InvalidAmountException {
        transactionManager = TransactionManager.getInstance();
        testCustomer = new RegularCustomer("John Doe", 30, "0241234567", "123 Main St, Accra");
        testAccount = new CheckingAccount(testCustomer, 1000.0);

        // Reset transaction count for isolated tests
        resetTransactionManager();
    }

    @AfterEach
    public void tearDown() {
        // Clean up after each test
        resetTransactionManager();
    }

    /**
     * Helper method to reset the transaction manager using its public API
     */
    private void resetTransactionManager() {
        try {
            transactionManager.clearTransactions();
        } catch (Exception e) {
            System.err.println("Warning: Could not reset TransactionManager: " + e.getMessage());
        }
    }

    // ==================== SINGLETON PATTERN TESTS ====================

    @Test
    @DisplayName("Should return singleton instance")
    public void testGetInstance_ReturnsSingleton() {
        TransactionManager instance1 = TransactionManager.getInstance();
        TransactionManager instance2 = TransactionManager.getInstance();

        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2);
    }

    @Test
    @DisplayName("Singleton instance should be consistent across calls")
    public void testGetInstance_ConsistentAcrossCalls() {
        TransactionManager instance1 = TransactionManager.getInstance();

        // Add a transaction
        Transaction txn = new Transaction("ACC001", "Deposit", 100.0, 1100.0);
        instance1.addTransaction(txn);

        // Get instance again
        TransactionManager instance2 = TransactionManager.getInstance();

        // Should have same transaction count
        assertEquals(instance1.getTransactionCount(), instance2.getTransactionCount());
        assertSame(instance1, instance2);
    }

    // ==================== ADD TRANSACTION TESTS ====================

    @Test
    @DisplayName("Should add valid transaction successfully")
    public void testAddTransaction_ValidTransaction() {
        Transaction transaction = new Transaction("ACC001", "Deposit", 100.0, 1100.0);

        int initialCount = transactionManager.getTransactionCount();
        transactionManager.addTransaction(transaction);

        assertEquals(initialCount + 1, transactionManager.getTransactionCount());
    }

    @Test
    @DisplayName("Should add multiple transactions")
    public void testAddTransaction_MultipleTransactions() {
        Transaction txn1 = new Transaction("ACC001", "Deposit", 100.0, 1100.0);
        Transaction txn2 = new Transaction("ACC001", "Withdrawal", 50.0, 1050.0);
        Transaction txn3 = new Transaction("ACC002", "Deposit", 200.0, 200.0);

        int initialCount = transactionManager.getTransactionCount();

        transactionManager.addTransaction(txn1);
        transactionManager.addTransaction(txn2);
        transactionManager.addTransaction(txn3);

        assertEquals(initialCount + 3, transactionManager.getTransactionCount());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when adding null transaction")
    public void testAddTransaction_NullTransaction() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionManager.addTransaction(null)
        );

        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    @DisplayName("Should support dynamic growth beyond 200 transactions without error")
    public void testAddTransaction_NoStorageLimitWithArrayList() {
        int toAdd = 205;
        assertDoesNotThrow(() -> {
            for (int i = 0; i < toAdd; i++) {
                Transaction txn = new Transaction("ACC001", "Deposit", 10.0, 1000.0 + (i * 10));
                transactionManager.addTransaction(txn);
            }
        });
        assertEquals(toAdd, transactionManager.getTransactionCount());
    }

    @Test
    @DisplayName("Should maintain correct count after adding transactions")
    public void testAddTransaction_MaintainsCorrectCount() {
        int initialCount = transactionManager.getTransactionCount();

        for (int i = 1; i <= 10; i++) {
            Transaction txn = new Transaction("ACC001", "Deposit", i * 10.0, 1000.0 + (i * 10));
            transactionManager.addTransaction(txn);
            assertEquals(initialCount + i, transactionManager.getTransactionCount());
        }
    }

    // ==================== GET TRANSACTION COUNT TESTS ====================

    @Test
    @DisplayName("Should return zero for empty transaction manager")
    public void testGetTransactionCount_EmptyManager() {
        assertEquals(0, transactionManager.getTransactionCount());
    }

    @Test
    @DisplayName("Should return correct count after adding transactions")
    public void testGetTransactionCount_AfterAddingTransactions() {
        Transaction txn1 = new Transaction("ACC001", "Deposit", 100.0, 1100.0);
        Transaction txn2 = new Transaction("ACC001", "Withdrawal", 50.0, 1050.0);

        transactionManager.addTransaction(txn1);
        assertEquals(1, transactionManager.getTransactionCount());

        transactionManager.addTransaction(txn2);
        assertEquals(2, transactionManager.getTransactionCount());
    }

    @Test
    @DisplayName("Should not change count when adding null transaction fails")
    public void testGetTransactionCount_AfterFailedAdd() {
        int initialCount = transactionManager.getTransactionCount();

        try {
            transactionManager.addTransaction(null);
        } catch (IllegalArgumentException e) {
            // Expected
        }

        assertEquals(initialCount, transactionManager.getTransactionCount());
    }

    // ==================== SORT TRANSACTIONS TESTS ====================

    @Test
    @DisplayName("Should sort transactions by timestamp in descending order")
    public void testSortTransactions_DescendingOrder() throws Exception {
        // Add transactions with delays to ensure different timestamps
        Transaction txn1 = new Transaction("ACC001", "Deposit", 100.0, 1100.0);
        Thread.sleep(10); // Small delay
        Transaction txn2 = new Transaction("ACC001", "Withdrawal", 50.0, 1050.0);
        Thread.sleep(10);
        Transaction txn3 = new Transaction("ACC001", "Deposit", 200.0, 1250.0);

        transactionManager.addTransaction(txn1);
        transactionManager.addTransaction(txn2);
        transactionManager.addTransaction(txn3);

        transactionManager.sortTransactionsByDate();

        // Verify order using public getters (most recent first)
        Transaction first = transactionManager.getTransaction(0);
        Transaction second = transactionManager.getTransaction(1);
        Transaction third = transactionManager.getTransaction(2);

        assertTrue(!first.getTimestamp().isBefore(second.getTimestamp()));
        assertTrue(!second.getTimestamp().isBefore(third.getTimestamp()));
    }

    @Test
    @DisplayName("Should handle sorting empty transaction list")
    public void testSortTransactions_EmptyList() {
        // Should not throw exception
        assertDoesNotThrow(() -> transactionManager.sortTransactionsByDate());
        assertEquals(0, transactionManager.getTransactionCount());
    }

    @Test
    @DisplayName("Should handle sorting single transaction")
    public void testSortTransactions_SingleTransaction() {
        Transaction txn = new Transaction("ACC001", "Deposit", 100.0, 1100.0);
        transactionManager.addTransaction(txn);

        assertDoesNotThrow(() -> transactionManager.sortTransactionsByDate());
        assertEquals(1, transactionManager.getTransactionCount());
    }

    @Test
    @DisplayName("Should maintain count after sorting")
    public void testSortTransactions_MaintainsCount() throws Exception {
        Transaction txn1 = new Transaction("ACC001", "Deposit", 100.0, 1100.0);
        Thread.sleep(10);
        Transaction txn2 = new Transaction("ACC001", "Withdrawal", 50.0, 1050.0);
        Thread.sleep(10);
        Transaction txn3 = new Transaction("ACC001", "Deposit", 200.0, 1250.0);

        transactionManager.addTransaction(txn1);
        transactionManager.addTransaction(txn2);
        transactionManager.addTransaction(txn3);

        int countBeforeSort = transactionManager.getTransactionCount();
        transactionManager.sortTransactionsByDate();
        int countAfterSort = transactionManager.getTransactionCount();

        assertEquals(countBeforeSort, countAfterSort);
    }

    // ==================== GET TRANSACTION TESTS (NEW METHOD) ====================

    @Test
    @DisplayName("Should get transaction by valid index")
    public void testGetTransaction_ValidIndex() {
        Transaction txn1 = new Transaction("ACC001", "Deposit", 100.0, 1100.0);
        Transaction txn2 = new Transaction("ACC001", "Withdrawal", 50.0, 1050.0);

        transactionManager.addTransaction(txn1);
        transactionManager.addTransaction(txn2);

        Transaction retrieved = transactionManager.getTransaction(0);
        assertNotNull(retrieved);
        assertEquals("ACC001", retrieved.getAccountNumber());
    }

    @Test
    @DisplayName("Should throw IndexOutOfBoundsException for negative index")
    public void testGetTransaction_NegativeIndex() {
        Transaction txn = new Transaction("ACC001", "Deposit", 100.0, 1100.0);
        transactionManager.addTransaction(txn);

        IndexOutOfBoundsException exception = assertThrows(
                IndexOutOfBoundsException.class,
                () -> transactionManager.getTransaction(-1)
        );

        assertTrue(exception.getMessage().contains("Invalid transaction index"));
    }

    @Test
    @DisplayName("Should throw IndexOutOfBoundsException for index >= count")
    public void testGetTransaction_IndexOutOfBounds() {
        Transaction txn = new Transaction("ACC001", "Deposit", 100.0, 1100.0);
        transactionManager.addTransaction(txn);

        int count = transactionManager.getTransactionCount();

        IndexOutOfBoundsException exception = assertThrows(
                IndexOutOfBoundsException.class,
                () -> transactionManager.getTransaction(count)
        );

        assertTrue(exception.getMessage().contains("Invalid transaction index"));
    }

    @Test
    @DisplayName("Should throw IndexOutOfBoundsException when getting from empty manager")
    public void testGetTransaction_EmptyManager() {
        IndexOutOfBoundsException exception = assertThrows(
                IndexOutOfBoundsException.class,
                () -> transactionManager.getTransaction(0)
        );
    }

    // ==================== INTEGRATION TESTS WITH ACCOUNTS ====================

    @Test
    @DisplayName("Should record deposit transaction from checking account")
    public void testIntegration_RecordDepositFromCheckingAccount() throws InvalidAmountException {
        CheckingAccount account = new CheckingAccount(testCustomer, 1000.0);
        int initialCount = transactionManager.getTransactionCount();

        account.deposit(100.0);

        // Should have 2 transactions: initial deposit + new deposit
        assertTrue(transactionManager.getTransactionCount() > initialCount);
    }

    @Test
    @DisplayName("Should record withdrawal transaction from checking account")
    public void testIntegration_RecordWithdrawalFromCheckingAccount() throws InvalidAmountException, OverdraftExceededException, InsufficientFundsException {
        CheckingAccount account = new CheckingAccount(testCustomer, 1000.0);
        int initialCount = transactionManager.getTransactionCount();

        account.withdraw(100.0);

        assertTrue(transactionManager.getTransactionCount() > initialCount);
    }

    @Test
    @DisplayName("Should record transactions from savings account")
    public void testIntegration_RecordFromSavingsAccount() throws InvalidAmountException {
        SavingsAccount account = new SavingsAccount(testCustomer, 1000.0);
        int countAfterCreation = transactionManager.getTransactionCount();

        account.deposit(200.0);

        assertTrue(transactionManager.getTransactionCount() > countAfterCreation);
    }

    @Test
    @DisplayName("Should record transfer transactions")
    public void testIntegration_RecordTransferTransactions() throws Exception {
        Customer customer2 = new RegularCustomer("Jane Smith", 35, "0551234567", "456 Oak Ave");
        CheckingAccount sender = new CheckingAccount(testCustomer, 1000.0);
        CheckingAccount receiver = new CheckingAccount(customer2, 500.0);

        int initialCount = transactionManager.getTransactionCount();

        sender.withdrawWithType(100.0, "Transfer Out");
        receiver.depositWithType(100.0, "Transfer In");

        // Should have recorded both transfer out and transfer in
        assertTrue(transactionManager.getTransactionCount() >= initialCount + 2);
    }

    // ==================== STRESS TESTS ====================

    @Test
    @DisplayName("Should handle adding transactions up to capacity")
    public void testStress_AddUpToCapacity() {
        int capacity = 200;

        for (int i = 0; i < capacity; i++) {
            Transaction txn = new Transaction("ACC001", "Deposit", 10.0, 1000.0 + (i * 10));
            transactionManager.addTransaction(txn);
        }

        assertEquals(capacity, transactionManager.getTransactionCount());
    }

    @Test
    @DisplayName("Should handle sorting large number of transactions")
    public void testStress_SortLargeNumberOfTransactions() throws Exception {
        int numTransactions = 100;

        for (int i = 0; i < numTransactions; i++) {
            Transaction txn = new Transaction("ACC001", "Deposit", 10.0, 1000.0 + (i * 10));
            transactionManager.addTransaction(txn);
            if (i % 10 == 0) {
                Thread.sleep(1); // Ensure different timestamps
            }
        }

        assertDoesNotThrow(() -> transactionManager.sortTransactionsByDate());
        assertEquals(numTransactions, transactionManager.getTransactionCount());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Should handle transactions with same timestamp")
    public void testEdgeCase_SameTimestamp() {
        Transaction txn1 = new Transaction("ACC001", "Deposit", 100.0, 1100.0);
        Transaction txn2 = new Transaction("ACC002", "Deposit", 200.0, 200.0);
        Transaction txn3 = new Transaction("ACC003", "Deposit", 300.0, 300.0);

        // Add quickly to potentially get same timestamp
        transactionManager.addTransaction(txn1);
        transactionManager.addTransaction(txn2);
        transactionManager.addTransaction(txn3);

        assertDoesNotThrow(() -> transactionManager.sortTransactionsByDate());
        assertEquals(3, transactionManager.getTransactionCount());
    }

    @Test
    @DisplayName("Should handle transactions for different accounts")
    public void testEdgeCase_DifferentAccounts() {
        Transaction txn1 = new Transaction("ACC001", "Deposit", 100.0, 1100.0);
        Transaction txn2 = new Transaction("ACC002", "Withdrawal", 50.0, 450.0);
        Transaction txn3 = new Transaction("ACC003", "Transfer In", 75.0, 575.0);
        Transaction txn4 = new Transaction("ACC001", "Transfer Out", 75.0, 1025.0);

        transactionManager.addTransaction(txn1);
        transactionManager.addTransaction(txn2);
        transactionManager.addTransaction(txn3);
        transactionManager.addTransaction(txn4);

        assertEquals(4, transactionManager.getTransactionCount());
    }

    @Test
    @DisplayName("Should handle different transaction types")
    public void testEdgeCase_DifferentTransactionTypes() {
        String[] types = {"Deposit", "Withdrawal", "Transfer In", "Transfer Out"};

        for (String type : types) {
            Transaction txn = new Transaction("ACC001", type, 100.0, 1000.0);
            transactionManager.addTransaction(txn);
        }

        assertEquals(types.length, transactionManager.getTransactionCount());
    }

    // ==================== CONCURRENCY SAFETY TESTS (BASIC) ====================

    @Test
    @DisplayName("Should maintain singleton across multiple thread accesses")
    public void testConcurrency_SingletonConsistency() throws InterruptedException {
        final TransactionManager[] instances = new TransactionManager[10];

        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                instances[index] = TransactionManager.getInstance();
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // All instances should be the same
        for (int i = 1; i < instances.length; i++) {
            assertSame(instances[0], instances[i]);
        }
    }

    // ==================== TRANSACTION DATA INTEGRITY TESTS ====================

    @Test
    @DisplayName("Should preserve transaction data after adding")
    public void testDataIntegrity_PreserveTransactionData() {
        Transaction original = new Transaction("ACC001", "Deposit", 100.0, 1100.0);
        String originalId = original.getTransactionId();
        String originalAccountNumber = original.getAccountNumber();
        String originalType = original.getType();
        double originalAmount = original.getAmount();
        double originalBalance = original.getBalanceAfter();

        transactionManager.addTransaction(original);

        // Transaction data should remain unchanged
        assertEquals(originalId, original.getTransactionId());
        assertEquals(originalAccountNumber, original.getAccountNumber());
        assertEquals(originalType, original.getType());
        assertEquals(originalAmount, original.getAmount(), 0.01);
        assertEquals(originalBalance, original.getBalanceAfter(), 0.01);
    }

    @Test
    @DisplayName("Should not modify transaction after sorting")
    public void testDataIntegrity_NoModificationAfterSort() throws Exception {
        Transaction txn1 = new Transaction("ACC001", "Deposit", 100.0, 1100.0);
        Thread.sleep(10);
        Transaction txn2 = new Transaction("ACC001", "Withdrawal", 50.0, 1050.0);

        String id1 = txn1.getTransactionId();
        double amount1 = txn1.getAmount();

        transactionManager.addTransaction(txn1);
        transactionManager.addTransaction(txn2);

        transactionManager.sortTransactionsByDate();

        // Original transaction data should be unchanged
        assertEquals(id1, txn1.getTransactionId());
        assertEquals(amount1, txn1.getAmount(), 0.01);
    }

    @Test
    @DisplayName("Test deposit transaction")
    public void testDepositTransaction() throws Exception {
        double initialBalance = testAccount.getBalance();
        double depositAmount = 500.0;

        testAccount.processTransaction(depositAmount, "Deposit");

        assertEquals(initialBalance + depositAmount, testAccount.getBalance());
    }

    @Test
    @DisplayName("Test withdrawal transaction")
    public void testWithdrawalTransaction() throws Exception {
        double initialBalance = testAccount.getBalance();
        double withdrawalAmount = 200.0;

        testAccount.processTransaction(withdrawalAmount, "Withdrawal");

        assertEquals(initialBalance - withdrawalAmount, testAccount.getBalance());
    }

    @Test
    @DisplayName("Test overdraft limit exceeded")
    public void testOverdraftLimitExceeded() {
        double withdrawalAmount = testAccount.getBalance() + 2000.0;

        OverdraftExceededException exception = assertThrows(
            OverdraftExceededException.class,
            () -> testAccount.processTransaction(withdrawalAmount, "Withdrawal")
        );

        assertTrue(exception.getMessage().contains("Overdraft limit exceeded"));
    }
}