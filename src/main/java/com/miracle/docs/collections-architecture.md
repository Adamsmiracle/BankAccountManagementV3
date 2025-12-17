# Bank Account Management System

A robust Java application for managing bank accounts, customers, and transactions with thread-safe operations and data persistence.

## Features

### Core Functionality
- **Account Management**
    - Create and manage Savings and Checking accounts
    - View account details and balances
    - Close or freeze accounts
- **Transaction Processing**
    - Deposit, withdraw, and transfer funds
    - Transaction history and statements
    - Support for multiple account types
- **Customer Management**
    - Customer profiles with personal information
    - Multiple accounts per customer
    - Customer type classification (Regular/Premium)
- **Data Persistence**
    - Save/Load accounts and transactions to/from files
    - Transaction logging
    - Data integrity checks

### Advanced Features
- **Concurrency & Thread Safety**
    - Thread-safe account operations
    - Parallel stream processing for balance calculations
    - Concurrent transaction handling
- **Exception Handling**
    - Custom exceptions for business rules
    - Graceful error recovery
    - Detailed error messages
- **Input Validation**
    - Data type and range validation
    - Business rule enforcement
    - Sanitization of user inputs

## Prerequisites

- Java 17 or higher
- Maven 3.8+

## Installation

1. Clone the repository:
   ```bash
   git clone [https://github.com/yourusername/BankAccountManagementV3.git](https://github.com/yourusername/BankAccountManagementV3.git)
   cd BankAccountManagementV3
Build the project:

mvn clean package

File Structure
src/
├── main/java/com/miracle/
│   ├── models/           # Domain models
│   ├── services/         # Business logic
│   ├── utils/            # Utility classes
│   ├── exceptions/       # Custom exceptions
│   └── Main.java         # Entry point
├── test/                 # Test classes
└── data/                 # Data storage
├── accounts.txt
└── transactions.txt