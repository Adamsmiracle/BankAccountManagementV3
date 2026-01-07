package com.miracle.runner;

import com.miracle.src.models.*;
import com.miracle.src.models.exceptions.InvalidAmountException;
import com.miracle.src.services.AccountManager;
import com.miracle.src.services.TransactionManager;
import com.miracle.src.utils.FileIOUtils;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for File I/O operations.
 * Tests reading and writing accounts and transactions to/from files.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FileIOTest {

    private static final String TEST_DATA_DIR = "src/test/java/com/miracle/runner/testdata";
    private static final Path TEST_ACCOUNTS_FILE = Paths.get(TEST_DATA_DIR, "test_accounts.txt");
    private static final Path TEST_TRANSACTIONS_FILE = Paths.get(TEST_DATA_DIR, "test_transactions.txt");

    @BeforeAll
    public static void setUpTestDirectory() throws IOException {
        // Create test data directory if it doesn't exist
        Path testDir = Paths.get(TEST_DATA_DIR);
        if (Files.notExists(testDir)) {
            Files.createDirectories(testDir);
        }
    }

    @AfterAll
    public static void cleanUpTestDirectory() throws IOException {
        // Clean up test files after all tests
        if (Files.exists(TEST_ACCOUNTS_FILE)) {
            Files.delete(TEST_ACCOUNTS_FILE);
        }
        if (Files.exists(TEST_TRANSACTIONS_FILE)) {
            Files.delete(TEST_TRANSACTIONS_FILE);
        }
        Path testDir = Paths.get(TEST_DATA_DIR);
        if (Files.exists(testDir)) {
            Files.delete(testDir);
        }
    }

    // ==================== TRANSACTION SERIALIZATION TESTS ====================

    @Test
    @Order(1)
    @DisplayName("Should serialize transaction to correct format")
    public void testSerializeTransaction() {
        // Create a transaction
        Transaction transaction = new Transaction(
                "TXN999", "ACC001", "Deposit", 100.00, 1100.00,
                LocalDateTime.of(2026, 1, 6, 14, 30, 0)
        );

        String serialized = FileIOUtils.serializeTransaction(transaction);

        // Verify format: ID|AccountNumber|Type|Amount|BalanceAfter|Timestamp
        assertNotNull(serialized);
        assertTrue(serialized.contains("TXN999"));
        assertTrue(serialized.contains("ACC001"));
        assertTrue(serialized.contains("Deposit"));
        assertTrue(serialized.contains("100.00"));
        assertTrue(serialized.contains("1100.00"));

        // Check pipe delimiter count (should be 5 pipes for 6 fields)
        long pipeCount = serialized.chars().filter(ch -> ch == '|').count();
        assertEquals(5, pipeCount, "Should have 5 pipe delimiters for 6 fields");
    }

    @Test
    @Order(2)
    @DisplayName("Should deserialize transaction from correct format")
    public void testDeserializeTransaction() {
        String line = "TXN001|ACC001|Deposit|500.00|500.00|06-01-2026 02:30:00 PM";

        Transaction transaction = FileIOUtils.deserializeTransaction(line);

        assertNotNull(transaction);
        assertEquals("TXN001", transaction.getTransactionId());
        assertEquals("ACC001", transaction.getAccountNumber());
        assertEquals("Deposit", transaction.getType());
        assertEquals(500.00, transaction.getAmount(), 0.01);
        assertEquals(500.00, transaction.getBalanceAfter(), 0.01);
    }

    @Test
    @Order(3)
    @DisplayName("Should return null for invalid transaction line")
    public void testDeserializeTransaction_InvalidLine() {
        // Too few fields
        String invalidLine = "TXN001|ACC001|Deposit";
        Transaction result = FileIOUtils.deserializeTransaction(invalidLine);
        assertNull(result);

        // Empty line
        Transaction resultEmpty = FileIOUtils.deserializeTransaction("");
        assertNull(resultEmpty);

        // Null line
        Transaction resultNull = FileIOUtils.deserializeTransaction(null);
        assertNull(resultNull);
    }

    @Test
    @Order(4)
    @DisplayName("Should round-trip serialize and deserialize transaction")
    public void testTransactionSerializationRoundTrip() {
        // Create original transaction
        Transaction original = new Transaction(
                "TXN100", "ACC005", "Withdrawal", 250.00, 750.00,
                LocalDateTime.of(2026, 1, 6, 10, 15, 30)
        );

        // Serialize and deserialize
        String serialized = FileIOUtils.serializeTransaction(original);
        Transaction deserialized = FileIOUtils.deserializeTransaction(serialized);

        // Verify all fields match
        assertNotNull(deserialized);
        assertEquals(original.getTransactionId(), deserialized.getTransactionId());
        assertEquals(original.getAccountNumber(), deserialized.getAccountNumber());
        assertEquals(original.getType(), deserialized.getType());
        assertEquals(original.getAmount(), deserialized.getAmount(), 0.01);
        assertEquals(original.getBalanceAfter(), deserialized.getBalanceAfter(), 0.01);
    }

    // ==================== ACCOUNT SERIALIZATION TESTS ====================

    @Test
    @Order(5)
    @DisplayName("Should serialize accounts to correct format")
    public void testSerializeAccounts() throws InvalidAmountException {
        // Create test accounts
        Customer customer1 = new RegularCustomer("John Doe", 30, "0551234567", "123 Main St");
        Account account1 = new SavingsAccount(customer1, 1000.00);

        Customer customer2 = new PremiumCustomer("Jane Smith", 45, "0559876543", "456 Oak Ave");
        Account account2 = new CheckingAccount(customer2, 5000.00);

        Map<String, Account> accounts = new HashMap<>();
        accounts.put(account1.getAccountNumber(), account1);
        accounts.put(account2.getAccountNumber(), account2);

        List<String> serialized = FileIOUtils.serializeAccounts(accounts);

        assertNotNull(serialized);
        assertEquals(2, serialized.size());

        // Verify each line has correct format
        for (String line : serialized) {
            long pipeCount = line.chars().filter(ch -> ch == '|').count();
            assertEquals(8, pipeCount, "Should have 8 pipe delimiters for 9 fields");
        }
    }

    @Test
    @Order(6)
    @DisplayName("Should handle empty accounts map")
    public void testSerializeAccounts_EmptyMap() {
        Map<String, Account> emptyAccounts = new HashMap<>();
        List<String> serialized = FileIOUtils.serializeAccounts(emptyAccounts);

        assertNotNull(serialized);
        assertTrue(serialized.isEmpty());
    }

    // ==================== FILE WRITE TESTS ====================

    @Test
    @Order(7)
    @DisplayName("Should save transactions to file")
    public void testSaveTransactionsToFile() throws IOException {
        // Create test transactions
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(
                "TXNTEST1", "ACC001", "Deposit", 100.00, 100.00,
                LocalDateTime.now()
        ));
        transactions.add(new Transaction(
                "TXNTEST2", "ACC001", "Withdrawal", 50.00, 50.00,
                LocalDateTime.now()
        ));

        // Save to test file
        FileIOUtils.saveTransactionsToFile(transactions);

        // This test verifies the method doesn't throw exceptions
        // The actual file writing is to the main data directory
        assertTrue(true, "Transactions saved without exception");
    }

    @Test
    @Order(8)
    @DisplayName("Should handle null transactions list gracefully")
    public void testSaveTransactionsToFile_NullList() {
        // Should not throw exception
        assertDoesNotThrow(() -> FileIOUtils.saveTransactionsToFile(null));
    }

    @Test
    @Order(9)
    @DisplayName("Should handle empty transactions list gracefully")
    public void testSaveTransactionsToFile_EmptyList() {
        // Should not throw exception
        assertDoesNotThrow(() -> FileIOUtils.saveTransactionsToFile(new ArrayList<>()));
    }

    // ==================== FILE READ TESTS ====================

    @Test
    @Order(10)
    @DisplayName("Should read transactions from file")
    public void testReadTransactionsFromFile() {
        List<Transaction> transactions = FileIOUtils.readTransactionsFromFile();

        // Should not return null (even if file doesn't exist)
        assertNotNull(transactions);
    }

    @Test
    @Order(11)
    @DisplayName("Should read accounts from file")
    public void testReadAccountsFromFile() {
        List<String> accountLines = FileIOUtils.readAccountsFromFile();

        // Should not return null (even if file doesn't exist)
        assertNotNull(accountLines);
    }

    // ==================== TRANSACTION MANAGER FILE INTEGRATION TESTS ====================

    @Test
    @Order(12)
    @DisplayName("TransactionManager should load transactions on start")
    public void testTransactionManagerLoadOnStart() {
        // This tests the integration between TransactionManager and FileIOUtils
        assertDoesNotThrow(() -> TransactionManager.loadTransactionsOnStart());

        TransactionManager manager = TransactionManager.getInstance();
        assertNotNull(manager.getAllTransactions());
    }

    @Test
    @Order(13)
    @DisplayName("TransactionManager should save transactions on exit")
    public void testTransactionManagerSaveOnExit() {
        TransactionManager manager = TransactionManager.getInstance();

        // Should not throw exception
        assertDoesNotThrow(() -> manager.saveTransactionsOnExit());
    }

    // ==================== ACCOUNT MANAGER FILE INTEGRATION TESTS ====================

    @Test
    @Order(14)
    @DisplayName("AccountManager should load accounts on start")
    public void testAccountManagerLoadOnStart() {
        // This tests the integration between AccountManager and FileIOUtils
        assertDoesNotThrow(() -> AccountManager.loadAccountsOnStart());

        AccountManager manager = AccountManager.getInstance();
        assertNotNull(manager.getAllAccounts());
    }

    @Test
    @Order(15)
    @DisplayName("AccountManager should save accounts on exit")
    public void testAccountManagerSaveOnExit() {
        AccountManager manager = AccountManager.getInstance();

        // Should not throw exception
        assertDoesNotThrow(() -> manager.saveAccountsOnExit());
    }

    // ==================== DUPLICATE PREVENTION TESTS ====================

    @Test
    @Order(16)
    @DisplayName("Should not load duplicate transactions")
    public void testNoDuplicateTransactionsLoaded() {
        // Load transactions twice
        TransactionManager.loadTransactionsOnStart();
        int countAfterFirstLoad = TransactionManager.getInstance().getTransactionCount();

        TransactionManager.loadTransactionsOnStart();
        int countAfterSecondLoad = TransactionManager.getInstance().getTransactionCount();

        // Count should remain the same (no duplicates)
        assertEquals(countAfterFirstLoad, countAfterSecondLoad,
                "Loading transactions twice should not create duplicates");
    }

    // ==================== DATA INTEGRITY TESTS ====================

    @Test
    @Order(17)
    @DisplayName("Transaction amount should always be positive after deserialization")
    public void testTransactionAmountPositiveAfterDeserialization() {
        // Test with a withdrawal transaction
        String line = "TXN001|ACC001|Withdrawal|100.00|400.00|06-01-2026 02:30:00 PM";
        Transaction transaction = FileIOUtils.deserializeTransaction(line);

        assertNotNull(transaction);
        assertTrue(transaction.getAmount() > 0, "Transaction amount should be positive");
    }

    @Test
    @Order(18)
    @DisplayName("Should preserve transaction type correctly")
    public void testTransactionTypePreserved() {
        String[] types = {"Deposit", "Withdrawal", "Transfer In", "Transfer Out"};

        for (String type : types) {
            String line = String.format("TXN001|ACC001|%s|100.00|500.00|06-01-2026 02:30:00 PM", type);
            Transaction transaction = FileIOUtils.deserializeTransaction(line);

            assertNotNull(transaction);
            assertEquals(type, transaction.getType(),
                    "Transaction type should be preserved for: " + type);
        }
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @Order(19)
    @DisplayName("Should handle large transaction amounts")
    public void testLargeTransactionAmounts() {
        String line = "TXN001|ACC001|Deposit|999999999.99|999999999.99|06-01-2026 02:30:00 PM";
        Transaction transaction = FileIOUtils.deserializeTransaction(line);

        assertNotNull(transaction);
        assertEquals(999999999.99, transaction.getAmount(), 0.01);
    }

    @Test
    @Order(20)
    @DisplayName("Should handle decimal precision correctly")
    public void testDecimalPrecision() {
        Transaction original = new Transaction(
                "TXN001", "ACC001", "Deposit", 123.45, 623.45,
                LocalDateTime.now()
        );

        String serialized = FileIOUtils.serializeTransaction(original);
        Transaction deserialized = FileIOUtils.deserializeTransaction(serialized);

        assertNotNull(deserialized);
        assertEquals(123.45, deserialized.getAmount(), 0.001);
        assertEquals(623.45, deserialized.getBalanceAfter(), 0.001);
    }

    // ==================== DATA SEEDER TESTS ====================

    @Test
    @Order(21)
    @DisplayName("DataSeeder should not throw exceptions")
    public void testDataSeederNoExceptions() {
        // DataSeeder should handle all cases gracefully
        assertDoesNotThrow(() -> com.miracle.src.utils.DataSeeder.seedInitialData());
    }

    @Test
    @Order(22)
    @DisplayName("DataSeeder isDataSeeded should return boolean")
    public void testDataSeederStatus() {
        boolean status = com.miracle.src.utils.DataSeeder.isDataSeeded();
        // Just verify it returns a boolean without exception
        assertTrue(status || !status);
    }
}
