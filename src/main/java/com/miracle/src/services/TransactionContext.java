// src/main/java/com/miracle/src/services/TransactionContext.java
package com.miracle.src.services;

/**
 * Deprecated. TransactionContext operations have been removed.
 * Concurrency and atomicity are handled via per-account synchronization
 * and thread-safe transaction logging in TransactionManager.
 */
@Deprecated
public final class TransactionContext {
    private TransactionContext() {}
}
