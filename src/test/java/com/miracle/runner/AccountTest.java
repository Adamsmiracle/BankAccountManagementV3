package com.miracle.runner;

import com.miracle.src.models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import com.miracle.src.models.exceptions.InvalidAmountException;
import com.miracle.src.models.exceptions.OverdraftExceededException;
import com.miracle.src.models.exceptions.InsufficientFundsException;

public class AccountTest extends Exception {

    private Customer regularCustomer;
    private Customer premiumCustomer;

    @BeforeEach
    public void setUp() {
        regularCustomer = new RegularCustomer("John Doe", 30, "0241234567", "123 Main St");
        premiumCustomer = new PremiumCustomer("Jane Smith", 35, "0551234567", "456 Oak Ave");
    }

    // ==================== CHECKING ACCOUNT TESTS ====================

    @Test
    public void testCheckingAccountCreation_ValidDeposit() throws InvalidAmountException {
        CheckingAccount account = new CheckingAccount(regularCustomer, 100.0);
        assertEquals(100.0, account.getBalance());
        assertEquals("Checking", account.getAccountType());
    }

    @Test
    public void testCheckingAccountCreation_InvalidDeposit_Zero() {
        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> new CheckingAccount(regularCustomer, 0.0)
        );
        assertTrue(exception.getMessage().contains("Invalid amount"));
    }

    @Test
    public void testCheckingAccountCreation_InvalidDeposit_Negative() {
        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> new CheckingAccount(regularCustomer, -50.0)
        );
        assertTrue(exception.getMessage().contains("Invalid amount"));
    }

    @Test
    public void testCheckingAccount_Deposit_Valid() throws InvalidAmountException {
        CheckingAccount account = new CheckingAccount(regularCustomer, 100.0);
        Transaction txn = account.deposit(50.0);

        assertNotNull(txn);
        assertEquals(150.0, account.getBalance());
        assertEquals("Deposit", txn.getType());
    }

    @Test
    public void testCheckingAccount_Deposit_InvalidAmount() throws InvalidAmountException {
        CheckingAccount account = new CheckingAccount(regularCustomer, 100.0);

        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> account.deposit(-10.0)
        );
        assertEquals(100.0, account.getBalance()); // Balance unchanged
    }

    @Test
    public void testCheckingAccount_Withdraw_Valid() throws InvalidAmountException, OverdraftExceededException, InsufficientFundsException {
        CheckingAccount account = new CheckingAccount(regularCustomer, 100.0);
        Transaction txn = account.withdraw(30.0);

        assertNotNull(txn);
        assertEquals(70.0, account.getBalance());
        assertEquals("Withdrawal", txn.getType());
    }

    @Test
    public void testCheckingAccount_Withdraw_WithinOverdraft() throws InvalidAmountException, OverdraftExceededException, InsufficientFundsException {
        CheckingAccount account = new CheckingAccount(regularCustomer, 50.0);
        Transaction txn = account.withdraw(100.0);

        assertNotNull(txn);
        assertEquals(-50.0, account.getBalance());
    }

    @Test
    public void testCheckingAccount_Withdraw_InvalidAmount() throws InvalidAmountException {
        CheckingAccount account = new CheckingAccount(regularCustomer, 100.0);

        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> account.withdraw(0.0)
        );
        assertEquals(100.0, account.getBalance());
    }

    @Test
    public void testCheckingAccount_Withdraw_NegativeAmount() throws InvalidAmountException {
        CheckingAccount account = new CheckingAccount(regularCustomer, 100.0);

        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> account.withdraw(-25.0)
        );
        assertEquals(100.0, account.getBalance());
    }

    // ==================== SAVINGS ACCOUNT TESTS ====================

    @Test
    public void testSavingsAccountCreation_ValidDeposit() throws InvalidAmountException {
        SavingsAccount account = new SavingsAccount(regularCustomer, 1000.0);
        assertEquals(1000.0, account.getBalance());
        assertEquals("Savings", account.getAccountType());
    }

    @Test
    public void testSavingsAccountCreation_InvalidDeposit() {
        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> new SavingsAccount(regularCustomer, 400.0)
        );
        assertTrue(exception.getMessage().contains("Initial deposit must be at least $500"));
    }


    @Test
    public void testSavingsAccountWithdrawal_MinimumBalanceViolation() throws InvalidAmountException {
        SavingsAccount account = new SavingsAccount(regularCustomer, 1000.0);
        double withdrawalAmount = 900.0;

        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> account.processTransaction(withdrawalAmount, "Withdrawal")
        );
        assertTrue(exception.getMessage().contains("below the minimum balance"));
    }

    @Test
    public void testSavingsAccount_Deposit_Valid() throws InvalidAmountException {
        SavingsAccount account = new SavingsAccount(regularCustomer, 600.0);
        Transaction txn = account.deposit(100.0);

        assertNotNull(txn);
        assertEquals(700.0, account.getBalance());
    }

    @Test
    public void testSavingsAccount_Deposit_InvalidAmount() throws InvalidAmountException {
        SavingsAccount account = new SavingsAccount(regularCustomer, 600.0);

        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> account.deposit(-50.0)
        );
        assertEquals(600.0, account.getBalance());
    }

    @Test
    public void testSavingsAccount_Withdraw_Valid() throws InvalidAmountException, InsufficientFundsException {
        SavingsAccount account = new SavingsAccount(regularCustomer, 1000.0);
        Transaction txn = account.withdraw(400.0);

        assertNotNull(txn);
        assertEquals(600.0, account.getBalance());
    }

    @Test
    public void testSavingsAccount_Withdraw_ViolatesMinimumBalance() throws InvalidAmountException {
        SavingsAccount account = new SavingsAccount(regularCustomer, 600.0);

        InsufficientFundsException exception = assertThrows(
                InsufficientFundsException.class,
                () -> account.withdraw(200.0) // Would result in 400, below 500 minimum
        );

        assertTrue(exception.getMessage().contains("minimum balance"));
        assertEquals(600.0, account.getBalance());
    }

    @Test
    public void testSavingsAccount_Withdraw_ExactlyAtMinimum() throws InvalidAmountException, InsufficientFundsException {
        SavingsAccount account = new SavingsAccount(regularCustomer, 600.0);

        // Should NOT throw exception
        Transaction t = account.withdraw(100.0); // Leaves exactly 500

        assertEquals(500.0, account.getBalance(), 0.01);
    }



    @Test
    public void testSavingsAccount_Withdraw_InvalidAmount() throws InvalidAmountException {
        SavingsAccount account = new SavingsAccount(regularCustomer, 1000.0);

        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> account.withdraw(0.0)
        );
        assertEquals(1000.0, account.getBalance());
    }

    // ==================== TRANSFER TESTS ====================

    @Test
    public void testTransfer_CheckingToChecking_Valid() throws InvalidAmountException, OverdraftExceededException {
        CheckingAccount sender = new CheckingAccount(regularCustomer, 200.0);
        CheckingAccount receiver = new CheckingAccount(premiumCustomer, 100.0);

        Transaction withdrawTxn = sender.withdrawWithType(50.0, "Transfer Out");
        Transaction depositTxn = receiver.depositWithType(50.0, "Transfer In");

        assertEquals(150.0, sender.getBalance());
        assertEquals(150.0, receiver.getBalance());
    }

    @Test
    public void testTransfer_ExceedsOverdraft() throws InvalidAmountException {
        CheckingAccount sender = new CheckingAccount(regularCustomer, 50.0);

        OverdraftExceededException exception = assertThrows(
                OverdraftExceededException.class,
                () -> sender.withdrawWithType(200.0, "Transfer Out")
        );

        assertEquals(50.0, sender.getBalance());
    }

    @Test
    public void testTransfer_SavingsViolatesMinimum() throws InvalidAmountException {
        SavingsAccount sender = new SavingsAccount(regularCustomer, 700.0);

        InsufficientFundsException exception = assertThrows(
                InsufficientFundsException.class,
                () -> sender.withdrawWithType(300.0, "Transfer Out")
        );

        assertEquals(700.0, sender.getBalance());
    }

    // ==================== PROCESS TRANSACTION TESTS ====================

    @Test
    public void testProcessTransaction_InvalidType() throws InvalidAmountException {
        CheckingAccount account = new CheckingAccount(regularCustomer, 100.0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> account.processTransaction(50.0, "InvalidType")
        );

        assertTrue(exception.getMessage().contains("Invalid transaction type"));
    }

    @Test
    public void testProcessTransaction_NullType() throws InvalidAmountException {
        CheckingAccount account = new CheckingAccount(regularCustomer, 100.0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> account.processTransaction(50.0, null)
        );

        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    public void testProcessTransaction_EmptyType() throws InvalidAmountException {
        CheckingAccount account = new CheckingAccount(regularCustomer, 100.0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> account.processTransaction(50.0, "")
        );
    }

    @Test
    public void testProcessTransaction_NegativeAmount() throws InvalidAmountException {
        CheckingAccount account = new CheckingAccount(regularCustomer, 100.0);

        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> account.processTransaction(-50.0, "Deposit")
        );
    }

    // ==================== INTEREST CALCULATION TEST ====================

    @Test
    public void testSavingsAccount_CalculateInterest() throws InvalidAmountException {
        SavingsAccount account = new SavingsAccount(regularCustomer, 1000.0);
        double interest = account.calculateInterest();

        assertEquals(35.0, interest, 0.01); // 3.5% of 1000
    }

    // ==================== CUSTOMER VALIDATION TESTS ====================

    @Test
    public void testCustomerCreation_NullName() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RegularCustomer(null, 30, "0241234567", "123 Main St")
        );
        assertTrue(exception.getMessage().contains("name"));
    }

    @Test
    public void testCustomerCreation_EmptyName() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RegularCustomer("", 30, "0241234567", "123 Main St")
        );
    }

    @Test
    public void testCustomerCreation_InvalidAge_TooYoung() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RegularCustomer("John Doe", 15, "0241234567", "123 Main St")
        );
        assertTrue(exception.getMessage().contains("age"));
    }

    @Test
    public void testCustomerCreation_InvalidAge_TooOld() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RegularCustomer("John Doe", 130, "0241234567", "123 Main St")
        );
    }

    @Test
    public void testCustomerCreation_NullContact() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RegularCustomer("John Doe", 30, null, "123 Main St")
        );
        assertTrue(exception.getMessage().contains("contact"));
    }

    @Test
    public void testCustomerCreation_NullAddress() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new RegularCustomer("John Doe", 30, "0241234567", null)
        );
        assertTrue(exception.getMessage().contains("address"));
    }
}