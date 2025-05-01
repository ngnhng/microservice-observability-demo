## Double Entry Bookkeeping System Demo

### Account Management Service

* **Customer Service:** Manages `/customers` endpoints.
* **Account Service:** Manages `/accounts` endpoints (often nested under customers).
* **Transaction Service:** Manages `/transactions` and orchestrates interactions with Account/Customer services.

---

### **Account Management (Account Service)**

#### **`POST /customers/{customerId}/accounts`**

* **Description:** Opens a new bank account for an existing customer.
* **Path Parameter:** `customerId` (UUID)
* **Request Body:** Account details (`account_type`, `currency`). Initial balance usually 0.
* **Response Body:** Created account details (including `account_id`, `account_number`, `status='ACTIVE'`).
* **Observability:** Trace involves checking customer status first (Customer Service call?) then
  creating the account (Account Service -> DB Insert).

#### **`GET /accounts/{accountId}`**

* **Description:** Retrieves details for a specific account.
* **Path Parameter:** `accountId` (UUID)
* **Response Body:** Full account details (including balance, status, customer_id).
* **Observability:** Simple trace (API Gateway -> Account Service -> DB Select).

#### **`GET /customers/{customerId}/accounts`**

* **Description:** Lists all accounts belonging to a specific customer.
* **Path Parameter:** `customerId` (UUID)
* **Response Body:** Array of account details.
* **Observability:** Simple trace (API Gateway -> Account Service -> DB Select with WHERE clause).

#### **`GET /accounts/{accountId}/balance`**

* **Description:** Retrieves the current balance for a specific account.
* **Path Parameter:** `accountId` (UUID)
* **Response Body:** `{ "accountId": "...", "balance": 1234.56, "currency": "USD" }`
* **Observability:** Very common read operation. Trace: API Gateway -> Account Service -> DB Select (potentially
  optimized).

#### **`PATCH /accounts/{accountId}/status`**

* **Description:** Updates the status of an account (e.g., freeze, close).
* **Path Parameter:** `accountId` (UUID)
* **Request Body:** `{ "status": "FROZEN" }` or `{ "status": "CLOSED" }`
* **Response Body:** Updated account details.
* **Observability:** Trace: API Gateway -> Account Service -> DB Update. Shows state changes.

Okay, based on the revised PostgreSQL schema, here's a set of RESTful API endpoints that would effectively serve your
observability demo project. These endpoints represent common fintech operations, designed to trigger interactions
between potential microservices (Customer, Account, Transaction) and the database, thus generating interesting
observability data (traces, metrics, logs).

We'll assume potential microservice boundaries for clarity:

* **Customer Service:** Manages `/customers` endpoints.
* **Account Service:** Manages `/accounts` endpoints (often nested under customers).
* **Transaction Service:** Manages `/transactions` and orchestrates interactions with Account/Customer services.

---

**API Endpoint Suggestions:**

**1. Customer Management (Customer Service)**

* **`POST /customers`**
* **Description:** Creates a new customer.
* **Request Body:** Customer details (first\_name, last\_name, email, phone\_number, address). KYC/Risk might be set to
  defaults initially.
* **Response Body:** Created customer details (including `customer_id`, `created_at`, `updated_at`, default
  `kyc_status`, `risk_level`).
* **Observability:** Simple trace (API Gateway -> Customer Service -> DB Insert). Good for baseline metrics/logging.

* **`GET /customers/{customerId}`**
* **Description:** Retrieves details for a specific customer.
* **Path Parameter:** `customerId` (UUID)
* **Response Body:** Full customer details.
* **Observability:** Simple trace (API Gateway -> Customer Service -> DB Select).

* **`PUT /customers/{customerId}`**
* **Description:** Updates customer information (e.g., address, phone).
* **Path Parameter:** `customerId` (UUID)
* **Request Body:** Fields to update.
* **Response Body:** Updated customer details.
* **Observability:** Simple trace (API Gateway -> Customer Service -> DB Update). Demonstrates `updated_at` trigger.

* **`GET /customers?email={email}`**
* **Description:** Finds a customer by email address.
* **Query Parameter:** `email`
* **Response Body:** Array containing the matching customer (or empty array).
* **Observability:** Demonstrates querying/filtering (API Gateway -> Customer Service -> DB Select with WHERE clause).

**2. Account Management (Account Service)**

* **`POST /customers/{customerId}/accounts`**
* **Description:** Opens a new bank account for an existing customer.
* **Path Parameter:** `customerId` (UUID)
* **Request Body:** Account details (`account_type`, `currency`). Initial balance usually 0.
* **Response Body:** Created account details (including `account_id`, `account_number`, `status='ACTIVE'`).
* **Observability:** Trace involves potentially checking customer status first (Customer Service call?) then creating
  the account (Account Service -> DB Insert).

* **`GET /accounts/{accountId}`**
* **Description:** Retrieves details for a specific account.
* **Path Parameter:** `accountId` (UUID)
* **Response Body:** Full account details (including balance, status, customer\_id).
* **Observability:** Simple trace (API Gateway -> Account Service -> DB Select).

* **`GET /customers/{customerId}/accounts`**
* **Description:** Lists all accounts belonging to a specific customer.
* **Path Parameter:** `customerId` (UUID)
* **Response Body:** Array of account details.
* **Observability:** Simple trace (API Gateway -> Account Service -> DB Select with WHERE clause).

* **`GET /accounts/{accountId}/balance`**
* **Description:** Retrieves the current balance for a specific account.
* **Path Parameter:** `accountId` (UUID)
* **Response Body:** `{ "accountId": "...", "balance": 1234.56, "currency": "USD" }`
* **Observability:** Very common read operation. Trace: API Gateway -> Account Service -> DB Select (potentially
  optimized).

* **`PATCH /accounts/{accountId}/status`**
* **Description:** Updates the status of an account (e.g., freeze, close). Likely an admin operation.
* **Path Parameter:** `accountId` (UUID)
* **Request Body:** `{ "status": "FROZEN" }` or `{ "status": "CLOSED" }`
* **Response Body:** Updated account details.
* **Observability:** Trace: API Gateway -> Account Service -> DB Update. Shows state changes.

**3. Transaction Processing (Transaction Service)**

* **`POST /transactions`**
* **Description:** Initiates a financial transaction (the core operation).
* **Request Body (Example: Transfer):**
  ```json
  {
    "transaction_type": "TRANSFER",
    "source_account_id": "uuid-of-source-account",
    "destination_account_id": "uuid-of-destination-account",
    "amount": 100.50,
    "currency": "USD",
    "description": "Payment for services",
    "idempotency_key": "unique-key-for-this-request"
  }
  ```
* **Response Body:** Initial transaction details (including `transaction_id`, `status='PENDING'` or `'PROCESSING'`).
* **Observability:** **This is the most valuable endpoint for the demo.** It triggers a complex distributed trace:
  * API Gateway -> Transaction Service
  * Transaction Service -> Account Service (Get source account details/balance)
  * Transaction Service -> Account Service (Get destination account details)
  * Transaction Service -> Customer Service (Optional: Check KYC/Risk for source/dest customers)
  * Transaction Service -> Fraud Service (Optional: Assess risk)
  * Transaction Service -> DB (Insert `transactions` record - PENDING)
  * Transaction Service -> DB (Insert DEBIT `ledger_entries`)
  * Transaction Service -> DB (Insert CREDIT `ledger_entries`)
  * Transaction Service -> Account Service (Update source balance - DB UPDATE)
  * Transaction Service -> Account Service (Update destination balance - DB UPDATE)
  * Transaction Service -> DB (Update `transactions` record - COMPLETED/FAILED)
  * Generates logs/metrics at each step, including potential failures (insufficient funds, frozen account, fraud).

* **`GET /transactions/{transactionId}`**
* **Description:** Retrieves the details and current status of a specific transaction.
* **Path Parameter:** `transactionId` (UUID)
* **Response Body:** Full transaction details.
* **Observability:** Trace: API Gateway -> Transaction Service -> DB Select. Useful for checking the outcome of a
  `POST /transactions`.

* **`GET /accounts/{accountId}/transactions`**
* **Description:** Lists transactions associated with a specific account (both source and destination).
* **Path Parameter:** `accountId` (UUID)
* **Query Parameters (Optional):** `limit`, `offset` (pagination), `startDate`, `endDate`, `status`.
* **Response Body:** Array of transaction details.
* **Observability:** Trace: API Gateway -> Transaction Service -> DB Select (potentially complex query with OR and
  sorting/filtering). Good for showing database query performance in traces.

**4. Ledger (Optional - often internal or combined)**

* **`GET /accounts/{accountId}/ledger`**
* **Description:** Retrieves the raw ledger entries for an account (for audit/detailed history).
* **Path Parameter:** `accountId` (UUID)
* **Query Parameters (Optional):** `limit`, `offset`, `startDate`, `endDate`.
* **Response Body:** Array of ledger entry details.
* **Observability:** Trace: API Gateway -> Transaction Service (or Ledger Service) -> DB Select (querying
  `ledger_entries` table, likely indexed).

---


Yes, the revised schema (`fintech_schema_revised.sql`) is **well-suited** for demonstrating a double-entry bookkeeping
system, particularly the core mechanics you described.

Here's why:

1. **Explicit Debit/Credit Tracking (`ledger_entries` table):**

* The `ledger_entries` table is the cornerstone of the double-entry system in this schema.
* The `entry_type` column (using `ledger_entry_type_enum` with 'DEBIT' and 'CREDIT') directly models the fundamental
  concept of debits and credits.
* Each row in this table represents a single debit or credit entry against a specific account (`account_id`).

2. **Linking Entries to Transactions (`transaction_id`):**

* The `transaction_id` foreign key in `ledger_entries` links multiple debit and credit entries back to the single
  originating business event recorded in the `transactions` table.
* This perfectly models the principle that "every transaction is recorded twice" (or more, as long as debits equal
  credits). For example, a simple transfer transaction in the `transactions` table will result in *at least* two rows in
  `ledger_entries`: one DEBIT from the source account and one CREDIT to the destination account.

3. **Enabling Balance Verification:**

* The structure allows you to easily query and verify the core principle of double-entry: **For any
  given `transaction_id`, the sum of all DEBIT amounts in `ledger_entries` must equal the sum of all CREDIT amounts.**
* You can demonstrate this with a SQL query grouping by `transaction_id` and summing amounts based on `entry_type`.

4. **Reconciling Ledger with Account Balances:**

* The `balance` column in the `accounts` table represents the *current state* resulting from all the historical debits
  and credits recorded in `ledger_entries` for that `account_id`.
* You can demonstrate how the ledger entries drive the account balance. For any given `account_id`, the current
  `balance` should theoretically equal the sum of all CREDIT amounts minus the sum of all DEBIT amounts for that account
  in the `ledger_entries` table (plus any starting balance).

5. **Foundation for the Accounting Equation (Assets = Liabilities + Equity):**

* While the current `account_type_enum` focuses on banking accounts (which are typically *Assets*), the fundamental
  structure *supports* extending this to the full accounting equation.
* To fully demonstrate Assets = Liabilities + Equity, you would conceptually (or by adding more ENUM values to
  `account_type_enum`) treat different `accounts` records as representing different categories:
  *   **Assets:** Checking, Savings
  *   **Liabilities:** Loans Payable (if you added a LOAN account type that represents money owed *by* the company)
  *   **Equity:** Owner's Capital, Retained Earnings, Revenue Accounts, Expense Accounts (Revenue increases Equity,
  Expenses decrease Equity).
* A transaction like "Earned Revenue" would then be recorded as a DEBIT to an Asset account (e.g., Checking) and a
  CREDIT to a Revenue account (representing an increase in Equity).
* The schema's `ledger_entries` table handles this perfectly – it just records debits and credits against *any* account
  ID, regardless of its type. The *interpretation* of how those accounts roll up into Assets, Liabilities, and Equity
  happens at a higher level (e.g., in reporting queries).

**Conclusion:**

The schema provides the necessary structure (`ledger_entries` with debit/credit types linked to transactions and
accounts) to effectively demonstrate the core principles of double-entry bookkeeping: recording balanced debits and
credits for every transaction and tracking their impact on account balances. While demonstrating the full accounting
equation might require adding more diverse account types, the underlying mechanism is sound.

