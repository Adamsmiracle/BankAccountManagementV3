### Verdict
- US‑2.2 (Load Accounts on Startup): Met.
- US‑2.1 (Save Accounts to File on exit): Partially met (only new accounts are persisted; existing accounts’ updated state is not written).
- Technical requirements (java.nio.file with functional style and method references): Met.

### Evidence
- Startup loading
    - `Main.main(...)` calls `AccountManager.loadAccountsOnStart();` which calls `FileIOUtils.readAccountsFromFile()`.
    - `FileIOUtils.readAccountsFromFile()` uses `Files.lines(accountFile)` (java.nio), a stream pipeline, and a method reference: `lines.forEach(FileIOUtils::parseAccount);` to materialize accounts.
    - `parseAccount(...)` constructs an `AccountRequest` and calls `accountManager.createAccountFromFile(request)` to create the domain objects.

- Exit saving
    - `Main.executeChoice(5)` calls `accountManager.saveAccountsOnExit();`.
    - `AccountManager.saveAccountsOnExit()` collects only `newlyCreatedAccountNumbers` and writes them via `FileIOUtils.appendAccountsToFile(newAccounts)`.
    - `appendAccountsToFile(...)` uses `Files.write(..., CREATE, APPEND)` to write to `src/main/java/com/miracle/data/accounts.txt`.
    - There is a full‑file writer `FileIOUtils.saveAccountToFile(Map<String, Account>)` that safely overwrites with backup, but it is not used anywhere.

- Technical requirements
    - Uses `java.nio.file.Files`/`Paths` throughout, plus streams and method references (`FileIOUtils::parseAccount`, `FileIOUtils::serializeTransaction`, etc.).

### Gaps vs. US‑2.1
- The current save‑on‑exit persists only newly created accounts. Any updates to existing accounts (e.g., balance changes, customer detail edits) are not written back to `accounts.txt` on exit. After restart, those updates will be lost because load uses only `accounts.txt` and does not replay transactions to recompute balances.
- Robustness: `appendAccountsToFile(...)` does not create the data directory if missing (contrast: transactions path calls `ensureDataDirExists()`). This can throw if `src/main/java/com/miracle/data` doesn’t exist.

### Recommendation to fully satisfy US‑2.1
- On exit, write the complete, current snapshot of all accounts (not only new ones), ideally using the already‑implemented `FileIOUtils.saveAccountToFile(...)` which performs safe overwrite with a backup.
    - That means changing `AccountManager.saveAccountsOnExit()` to pass the full in‑memory accounts map instead of only `newAccounts`.
- Optionally, ensure the accounts data directory is created before writing (mirror the `ensureDataDirExists()` pattern used for transactions).

### Summary
- US‑2.2 and technical requirements: satisfied.
- US‑2.1 as written (“Write account data to accounts.txt on exit”): currently only partially satisfied, because only new accounts are saved, not the entire current account state.