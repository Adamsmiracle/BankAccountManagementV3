# 🐙 Project Git Workflow Documentation


## Phase 1: Clean Code Refactoring (`feature/refactor`)

This phase focuses on refactoring existing code for clarity, modularity, and clean code practices (US-2.1)
| Step                   | Command                                                                             |                                                         
|:-----------------------|:------------------------------------------------------------------------------------|
| **1. Create & Switch** | `git checkout -b feature/refactor`                                                  
| **2. Code Refactor**   | *Implement refactoring tasks on `AccountManager` and `TransactionManager`.*         
| **3. Commit Changes**  | `git add .` <br> `git commit -m "Refactored AccountManager and TransactionManager"` 



Phase 2: Exception Handling (`feature/exceptions`)
This phase focuses on defining custom exception classes
| **1. Create & Switch** | `git checkout -b feature/exceptions`
| **2. Code Exceptions** | *Create custom exceptions (`InvalidAmountException`, etc.)  |
| **3. Commit Changes** | `git commit -m "Implemented custom exceptions and input validation"`

## Phase 3: Testing and Verification (`feature/testing`)
This phase focuses on writing and verifying JUnit 5 unit tests

 Create & Switch** | `git checkout -b feature/testing` 
 Write Tests** | [cite_start]*Write `AccountTest.java`, `TransactionManagerTest.java`, and `ExceptionTest.java` in `src/test/java/`
 Commit Tests** | `git add src/test/java/` <br> `git commit -m "Added JUnit tests for deposit, withdraw, and transfer"`
 Find Refactor Hash** | `git checkout feature/refactor`
 Cherry-Pick** | `git checkout feature/testing` <br> `git cherry-pick`
 Run Tests

## Phase 4: Merge, Push,
This final phase integrates all features into the main branch and pushes the complete history to the remote repository[cite: 288].
| **1. Switch to Main** | `git checkout main` 
| **2. Merge Exceptions** | `git merge feature/exceptions`
| **3. Merge Testing** | `git merge feature/testing`
| **4. Resolve Conflicts** |
| **5. Push to Remote** | 