# BankAccountManagement
Amalitech java basics bank management project

# 🏦 Bank Account Management System

## Project Overview

This is a console-based Java application designed to simulate a basic bank
account management system. It provides core functionality for creating, viewing,
and managing customer accounts and their transactions, adhering strictly to
**Object-Oriented Programming (OOP)** principles and utilizing fundamental
**Data Structures and Algorithms (DSA)** for data management.

---

## ✨ Features

The application provides the following core functions via a simple command-line
interface:

### Core Functionality
* **Create Account: Register new customer accounts (Savings or Checking).
* **View Accounts: Display a comprehensive list of all active accounts,
including total bank balance.
* View Account statement
* View all customers 
* View all transactions by the bank
* **Process Transaction: Handle deposits and withdrawals against a specific
* account.
* **View Transactions History: Display a chronological list of transactions
  (newest first) for any given account, including account-specific totals.
* **Simple Menu: Navigate all application options.

### Account Types
| Type               | Key Features |

| **Savings Account  | Earns interest (3.5% annually). Enforces a **minimum balance** of **\$500** on withdrawals. |
| **Checking Account | Does not earn interest. Has an **overdraft limit** of **\$1,000**. Applies a **\$10 monthly fee**. |

### Customer Types
| Type | Key Features |

| **Regular Customer | Standard banking services. |
| **Premium Customer | Enhanced benefits, including waived monthly
fees on Checking Accounts. |

---

##  Technical Design and Structure

The system is built on robust OOP principles using two primary inheritance
hierarchies (`Account` and `Customer`) and centralized management classes.

### OOP Principles Applied
*  Encapsulation: All class fields are declared as `private` and accessed only through `public` getters and setters.
* **Inheritance:** Used to define specialized **`Account` types (`SavingsAccount`, `CheckingAccount`) and **`Customer`** types (`RegularCustomer`, `PremiumCustomer`).
* **Abstraction:** Achieved through the `abstract class Account` and `abstract class Customer`, as well as the **`Transactable`** interface.
* **Polymorphism:** Utilized via method overriding, notably in the `deposit()`, `withdraw()`, and `displayAccountDetails()` methods.
* **Composition:** `AccountManager` manages an array of `Account` objects, and `TransactionManager` manages an array of `Transaction` objects.

### Required Classes and Interfaces (11 Files)

| Component | Role | Principles |

| **`Account`** | Abstract base class for all account types. Implements `Transactable`. | Abstraction, Inheritance |
| **`SavingsAccount`** | Concrete account class implementing savings rules. | Inheritance, Polymorphism |
| **`CheckingAccount`** | Concrete account class implementing checking rules. | Inheritance, Polymorphism |
| **`Customer`** | Abstract base class for all customer types. | Abstraction, Inheritance |
| **`RegularCustomer`** | Concrete customer class. | Inheritance |
| **`PremiumCustomer`** | Concrete customer class with fee-waiving capability. | Inheritance |
| **`Transaction`** | Data class for logging deposits and withdrawals. | Encapsulation |
| **`Transactable`** | Interface defining core transaction capabilities. | Abstraction, Interface |
| **`AccountManager`** | Singleton class to store and search all accounts. | Composition, Singleton, DSA |
| **`TransactionManager`** | Singleton class to store and manage all transactions. | Composition, Singleton, DSA |
| **`AccountService`** | Handles business logic (e.g., coordinating transactions). | Composition |
| **`Main`** | Application entry point and menu logic. | |

### Data Structures & Algorithms (DSA)
* **Data Structure:** Accounts and Transactions are managed using fixed-size Java Arrays
  within their respective Manager classes.
* **Search:** Linear search is applied in `AccountManager`
  to locate an account by its number (`findAccount`).
* **Sorting:** Transaction history is sorted in reverse
  chronological order (newest first)** before display.
* **ID Generation:** Unique IDs (`ACC###`, `CUS###`, `TXN###`)
  are generated using **static counters** in their respective classes.

---

## 🚀 Getting Started

### Prerequisites
* Java Development Kit (JDK) 8 or newer installed.
* A suitable Java IDE (e.g., IntelliJ IDEA, Eclipse) or a command-line environment.

### Setup and Running

1.  Compile the files:
    Open your terminal in the project's root directory and compile all `.java` files:
    ```bash
    javac *.java
    ```

2.  **Run the application:**
    Execute the `Main` class:
    ```bash
    java Main
    ```

The application will start, seed initial data, and present the main menu for interaction.