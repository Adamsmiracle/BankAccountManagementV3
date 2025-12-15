package com.miracle.runner;

import com.miracle.src.models.exceptions.AccountNotFoundException;
import com.miracle.src.models.exceptions.InsufficientFundsException;
import com.miracle.src.models.exceptions.InvalidAmountException;
import com.miracle.src.models.exceptions.OverdraftExceededException;
import com.miracle.src.models.exceptions.TransactionFailedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class ExceptionTest {

    // ==================== INVALID AMOUNT EXCEPTION TESTS ====================

    @Test
    @DisplayName("InvalidAmountException should contain correct amount in message")
    public void testInvalidAmountException_MessageContainsAmount() {
        InvalidAmountException exception = new InvalidAmountException(-50.0);

        assertTrue(exception.getMessage().contains("-50"));
        assertTrue(exception.getMessage().contains("Invalid amount"));
        assertEquals(-50.0, exception.getAmount(), 0.01);
    }

    @Test
    @DisplayName("InvalidAmountException should work with zero amount")
    public void testInvalidAmountException_ZeroAmount() {
        InvalidAmountException exception = new InvalidAmountException(0.0);

        assertTrue(exception.getMessage().contains("0"));
        assertEquals(0.0, exception.getAmount(), 0.01);
    }

    @Test
    @DisplayName("InvalidAmountException should work with custom message")
    public void testInvalidAmountException_CustomMessage() {
        InvalidAmountException exception = new InvalidAmountException(
                "Initial deposit must be at least $500",
                400.0
        );

        assertTrue(exception.getMessage().contains("Initial deposit"));
        assertTrue(exception.getMessage().contains("500"));
        assertEquals(400.0, exception.getAmount(), 0.01);
    }

    @Test
    @DisplayName("InvalidAmountException should store amount correctly")
    public void testInvalidAmountException_GetAmount() {
        InvalidAmountException exception1 = new InvalidAmountException(-100.5);
        assertEquals(-100.5, exception1.getAmount(), 0.01);

        InvalidAmountException exception2 = new InvalidAmountException(0.0);
        assertEquals(0.0, exception2.getAmount(), 0.01);

        InvalidAmountException exception3 = new InvalidAmountException("Custom", 250.75);
        assertEquals(250.75, exception3.getAmount(), 0.01);
    }

    @Test
    @DisplayName("InvalidAmountException should be an Exception (checked)")
    public void testInvalidAmountException_IsCheckedException() {
        InvalidAmountException exception = new InvalidAmountException(0.0);
        assertTrue(exception instanceof Exception);
    }

    // ==================== OVERDRAFT EXCEEDED EXCEPTION TESTS ====================

    @Test
    @DisplayName("OverdraftExceededException should contain all details in message")
    public void testOverdraftExceededException_MessageContainsAllDetails() {
        OverdraftExceededException exception = new OverdraftExceededException(
                50.0,    // balance
                200.0,   // withdrawal amount
                100.0    // overdraft limit
        );

        String message = exception.getMessage();
        assertTrue(message.contains("50"));      // balance
        assertTrue(message.contains("200"));     // withdrawal
        assertTrue(message.contains("100"));     // overdraft limit
        assertTrue(message.contains("-150") || message.contains("150")); // resulting balance
        assertTrue(message.contains("Overdraft"));
    }

    @Test
    @DisplayName("OverdraftExceededException should store balance correctly")
    public void testOverdraftExceededException_GetBalance() {
        OverdraftExceededException exception = new OverdraftExceededException(
                75.50, 250.0, 100.0
        );

        assertEquals(75.50, exception.getBalance(), 0.01);
    }

    @Test
    @DisplayName("OverdraftExceededException should store withdrawal amount correctly")
    public void testOverdraftExceededException_GetWithdrawalAmount() {
        OverdraftExceededException exception = new OverdraftExceededException(
                50.0, 225.75, 100.0
        );

        assertEquals(225.75, exception.getWithdrawalAmount(), 0.01);
    }

    @Test
    @DisplayName("OverdraftExceededException should store overdraft limit correctly")
    public void testOverdraftExceededException_GetOverdraftLimit() {
        OverdraftExceededException exception = new OverdraftExceededException(
                50.0, 200.0, 150.0
        );

        assertEquals(150.0, exception.getOverdraftLimit(), 0.01);
    }

    @Test
    @DisplayName("OverdraftExceededException should handle edge case at exact limit")
    public void testOverdraftExceededException_ExactLimit() {
        // Balance: 50, Withdrawal: 150, Limit: 100
        // Resulting: -100 (exactly at limit, should not throw in real scenario)
        OverdraftExceededException exception = new OverdraftExceededException(
                50.0, 150.0, 100.0
        );

        assertEquals(50.0, exception.getBalance(), 0.01);
        assertEquals(150.0, exception.getWithdrawalAmount(), 0.01);
        assertEquals(100.0, exception.getOverdraftLimit(), 0.01);
    }

    @Test
    @DisplayName("OverdraftExceededException should handle zero balance")
    public void testOverdraftExceededException_ZeroBalance() {
        OverdraftExceededException exception = new OverdraftExceededException(
                0.0, 150.0, 100.0
        );

        assertEquals(0.0, exception.getBalance(), 0.01);
        assertTrue(exception.getMessage().contains("150"));
    }

    @Test
    @DisplayName("OverdraftExceededException should handle negative balance")
    public void testOverdraftExceededException_NegativeBalance() {
        OverdraftExceededException exception = new OverdraftExceededException(
                -50.0, 100.0, 100.0
        );

        assertEquals(-50.0, exception.getBalance(), 0.01);
        assertEquals(100.0, exception.getWithdrawalAmount(), 0.01);
    }

    @Test
    @DisplayName("OverdraftExceededException should be an Exception (checked)")
    public void testOverdraftExceededException_IsCheckedException() {
        OverdraftExceededException exception = new OverdraftExceededException(
                50.0, 200.0, 100.0
        );
        assertTrue(exception instanceof Exception);
    }

    // ==================== INSUFFICIENT FUNDS EXCEPTION TESTS ====================

    @Test
    @DisplayName("InsufficientFundsException should store message correctly")
    public void testInsufficientFundsException_MessageStored() {
        String customMessage = "Withdrawal would violate minimum balance requirement";
        InsufficientFundsException exception = new InsufficientFundsException(customMessage);

        assertEquals(customMessage, exception.getMessage());
    }

    @Test
    @DisplayName("InsufficientFundsException should handle detailed message")
    public void testInsufficientFundsException_DetailedMessage() {
        String message = "Withdrawal failed. Resulting balance ($400.00) would violate " +
                "the minimum balance requirement ($500.00)";
        InsufficientFundsException exception = new InsufficientFundsException(message);

        assertTrue(exception.getMessage().contains("400"));
        assertTrue(exception.getMessage().contains("500"));
        assertTrue(exception.getMessage().contains("minimum balance"));
    }

    @Test
    @DisplayName("InsufficientFundsException should be a checked exception")
    public void testInsufficientFundsException_IsCheckedException() {
        Exception exception = new InsufficientFundsException("Test message");
        assertTrue(exception instanceof Exception);
        assertFalse(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("InsufficientFundsException should handle empty message")
    public void testInsufficientFundsException_EmptyMessage() {
        InsufficientFundsException exception = new InsufficientFundsException("");
        assertNotNull(exception.getMessage());
        assertEquals("", exception.getMessage());
    }

    @Test
    @DisplayName("InsufficientFundsException should handle null message")
    public void testInsufficientFundsException_NullMessage() {
        InsufficientFundsException exception = new InsufficientFundsException(null);
        assertNull(exception.getMessage());
    }

    // ==================== ACCOUNT NOT FOUND EXCEPTION TESTS ====================

    @Test
    @DisplayName("AccountNotFoundException should contain account number in message")
    public void testAccountNotFoundException_MessageContainsAccountNumber() {
        AccountNotFoundException exception = new AccountNotFoundException("ACC001");

        assertTrue(exception.getMessage().contains("ACC001"));
        assertTrue(exception.getMessage().contains("Account not found"));
    }

    @Test
    @DisplayName("AccountNotFoundException should work with custom message")
    public void testAccountNotFoundException_CustomMessage() {
        AccountNotFoundException exception = new AccountNotFoundException(
                "Cannot find account",
                "ACC999"
        );

        assertTrue(exception.getMessage().contains("Cannot find account"));
        assertTrue(exception.getMessage().contains("ACC999"));
    }

    @Test
    @DisplayName("AccountNotFoundException should handle invalid account numbers")
    public void testAccountNotFoundException_InvalidAccountNumber() {
        AccountNotFoundException exception1 = new AccountNotFoundException("INVALID123");
        assertTrue(exception1.getMessage().contains("INVALID123"));

        AccountNotFoundException exception2 = new AccountNotFoundException("");
        assertTrue(exception2.getMessage().contains("Account not found"));
    }

    @Test
    @DisplayName("AccountNotFoundException should contain correct account number in message")
    public void testAccountNotFoundException_MessageContainsAccountNumber2() {
        AccountNotFoundException exception = new AccountNotFoundException("ACC123");

        assertTrue(exception.getMessage().contains("ACC123"));
        assertTrue(exception.getMessage().contains("Account not found"));
    }

    // ==================== EXCEPTION HIERARCHY TESTS ====================

//    @Test
//    @DisplayName("Checked exceptions should extend Exception")
//    public void testCheckedExceptions_ExtendException() {
//        InvalidAmountException ex1 = new InvalidAmountException(0.0);
//        assertTrue(ex1 instanceof Exception);
//        assertFalse(ex1 instanceof RuntimeException);
//
//        OverdraftExceededException ex2 = new OverdraftExceededException(50, 200, 100);
//        assertTrue(ex2 instanceof Exception);
//        assertFalse(ex2 instanceof RuntimeException);
//    }

//    @Test
//    @DisplayName("Unchecked exceptions should extend RuntimeException")
//    public void testUncheckedExceptions_ExtendRuntimeException() {
//        InsufficientFundsException ex1 = new InsufficientFundsException("Test");
//        assertTrue(ex1 instanceof RuntimeException);
//        assertTrue(ex1 instanceof Exception);
//
//        AccountNotFoundException ex2 = new AccountNotFoundException("ACC001");
//        assertTrue(ex2 instanceof RuntimeException);
//        assertTrue(ex2 instanceof Exception);
//    }

    // ==================== EXCEPTION COMPARISON TESTS ====================

    @Test
    @DisplayName("Same exception types with same data should have different references")
    public void testExceptions_DifferentReferences() {
        InvalidAmountException ex1 = new InvalidAmountException(0.0);
        InvalidAmountException ex2 = new InvalidAmountException(0.0);

        assertNotSame(ex1, ex2);
    }

    @Test
    @DisplayName("Exception messages should be immutable")
    public void testExceptions_ImmutableMessages() {
        InvalidAmountException exception = new InvalidAmountException(-50.0);
        String originalMessage = exception.getMessage();

        // Try to get message multiple times
        assertEquals(originalMessage, exception.getMessage());
        assertEquals(originalMessage, exception.getMessage());
    }

    // ==================== EXCEPTION EDGE CASES ====================

    @Test
    @DisplayName("InvalidAmountException should handle very large amounts")
    public void testInvalidAmountException_LargeAmount() {
        InvalidAmountException exception = new InvalidAmountException(Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, exception.getAmount(), 0.01);
    }

    @Test
    @DisplayName("InvalidAmountException should handle very small amounts")
    public void testInvalidAmountException_SmallAmount() {
        InvalidAmountException exception = new InvalidAmountException(0.01);
        assertEquals(0.01, exception.getAmount(), 0.001);
    }

    @Test
    @DisplayName("OverdraftExceededException should handle decimal values")
    public void testOverdraftExceededException_DecimalValues() {
        OverdraftExceededException exception = new OverdraftExceededException(
                123.45,
                678.90,
                100.00
        );

        assertEquals(123.45, exception.getBalance(), 0.01);
        assertEquals(678.90, exception.getWithdrawalAmount(), 0.01);
        assertEquals(100.00, exception.getOverdraftLimit(), 0.01);
    }

    // ==================== EXCEPTION STACK TRACE TESTS ====================

    @Test
    @DisplayName("Exceptions should have stack traces")
    public void testExceptions_HaveStackTraces() {
        InvalidAmountException ex1 = new InvalidAmountException(0.0);
        assertNotNull(ex1.getStackTrace());
        assertTrue(ex1.getStackTrace().length > 0);

        OverdraftExceededException ex2 = new OverdraftExceededException(50, 200, 100);
        assertNotNull(ex2.getStackTrace());
        assertTrue(ex2.getStackTrace().length > 0);

        InsufficientFundsException ex3 = new InsufficientFundsException("Test");
        assertNotNull(ex3.getStackTrace());
        assertTrue(ex3.getStackTrace().length > 0);

        AccountNotFoundException ex4 = new AccountNotFoundException("ACC001");
        assertNotNull(ex4.getStackTrace());
        assertTrue(ex4.getStackTrace().length > 0);
    }

    // ==================== EXCEPTION THROWING AND CATCHING TESTS ====================

    @Test
    @DisplayName("Should be able to catch InvalidAmountException as Exception")
    public void testCatchInvalidAmountAsException() {
        try {
            throw new InvalidAmountException(0.0);
        } catch (Exception e) {
            assertTrue(e instanceof InvalidAmountException);
        }
    }

    @Test
    @DisplayName("Should be able to catch OverdraftExceededException as Exception")
    public void testCatchOverdraftExceededAsException() {
        try {
            throw new OverdraftExceededException(50, 200, 100);
        } catch (Exception e) {
            assertTrue(e instanceof OverdraftExceededException);
        }
    }

    @Test
    @DisplayName("Should be able to catch AccountNotFoundException as RuntimeException")
    public void testCatchAccountNotFoundAsRuntimeException() {
        try {
            throw new AccountNotFoundException("ACC001");
        } catch (AccountNotFoundException e) {
            assertTrue(e instanceof AccountNotFoundException);

        }
    }

    // ==================== EXCEPTION SERIALIZATION TESTS ====================

    @Test
    @DisplayName("Exceptions should be serializable")
    public void testExceptions_AreSerializable() {
        InvalidAmountException ex1 = new InvalidAmountException(0.0);
        assertTrue(ex1 instanceof java.io.Serializable);

        OverdraftExceededException ex2 = new OverdraftExceededException(50, 200, 100);
        assertTrue(ex2 instanceof java.io.Serializable);

        InsufficientFundsException ex3 = new InsufficientFundsException("Test");
        assertTrue(ex3 instanceof java.io.Serializable);

        AccountNotFoundException ex4 = new AccountNotFoundException("ACC001");
        assertTrue(ex4 instanceof java.io.Serializable);
    }

    // ==================== REAL-WORLD SCENARIO TESTS ====================

    @Test
    @DisplayName("Should handle complete withdrawal scenario with OverdraftExceededException")
    public void testRealWorldScenario_OverdraftExceeded() {
        double currentBalance = 75.0;
        double withdrawalAmount = 200.0;
        double overdraftLimit = 100.0;
        double resultingBalance = currentBalance - withdrawalAmount; // -125

        OverdraftExceededException exception = new OverdraftExceededException(
                currentBalance,
                withdrawalAmount,
                overdraftLimit
        );

        // Verify all details are correct
        assertEquals(currentBalance, exception.getBalance(), 0.01);
        assertEquals(withdrawalAmount, exception.getWithdrawalAmount(), 0.01);
        assertEquals(overdraftLimit, exception.getOverdraftLimit(), 0.01);

        // Verify resulting balance would violate overdraft
        assertTrue(resultingBalance < -overdraftLimit);

        // Verify message contains key information
        String message = exception.getMessage();
        assertTrue(message.contains("75") || message.contains("75.0"));
        assertTrue(message.contains("200") || message.contains("200.0"));
        assertTrue(message.contains("100") || message.contains("100.0"));
    }

    @Test
    @DisplayName("Should handle complete savings withdrawal scenario with InsufficientFundsException")
    public void testRealWorldScenario_InsufficientFunds() {
        double currentBalance = 600.0;
        double withdrawalAmount = 200.0;
        double minimumBalance = 500.0;
        double resultingBalance = currentBalance - withdrawalAmount; // 400

        String expectedMessage = String.format(
                "Withdrawal failed. Resulting balance ($%.2f) would violate " +
                        "the minimum balance requirement ($%.2f)",
                resultingBalance, minimumBalance
        );

        InsufficientFundsException exception = new InsufficientFundsException(expectedMessage);

        assertTrue(exception.getMessage().contains("400"));
        assertTrue(exception.getMessage().contains("500"));
        assertTrue(resultingBalance < minimumBalance);
    }

    @Test
    @DisplayName("Should handle account lookup failure with AccountNotFoundException")
    public void testRealWorldScenario_AccountNotFound() {
        String searchedAccountNumber = "ACC999";

        AccountNotFoundException exception = new AccountNotFoundException(searchedAccountNumber);

        assertTrue(exception.getMessage().contains(searchedAccountNumber));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("Should handle invalid deposit with InvalidAmountException")
    public void testRealWorldScenario_InvalidDeposit() {
        double invalidAmount = -100.0;

        InvalidAmountException exception = new InvalidAmountException(invalidAmount);

        assertEquals(invalidAmount, exception.getAmount(), 0.01);
        assertTrue(exception.getMessage().contains("-100"));
        assertTrue(exception.getMessage().contains("Invalid"));
    }

    @Test
    @DisplayName("TransactionFailedException should contain correct message")
    public void testTransactionFailedException_Message() {
        TransactionFailedException exception = new TransactionFailedException("Transaction failed");

        assertEquals("Transaction failed", exception.getMessage());
    }

    @Test
    @DisplayName("OverdraftExceededException should contain correct details in message")
    public void testOverdraftExceededException_MessageContainsDetails() {
        OverdraftExceededException exception = new OverdraftExceededException(100.0, 200.0, 50.0);

        assertTrue(exception.getMessage().contains("Overdraft limit exceeded"));
        assertTrue(exception.getMessage().contains("Balance: $100.00"));
        assertTrue(exception.getMessage().contains("Withdrawal: $200.00"));
        assertTrue(exception.getMessage().contains("overdraft limit of $50.00"));
    }
}