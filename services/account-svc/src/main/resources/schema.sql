-- Ensure necessary extensions are enabled (run once per database)
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp"; -- Alternative for UUID generation
-- CREATE EXTENSION IF NOT EXISTS "pgcrypto"; -- For gen_random_uuid()

-- ------------------------------------------
-- ENUM Type Definitions
-- ------------------------------------------

CREATE TYPE customer_kyc_status_enum AS ENUM ('PENDING', 'VERIFIED', 'REJECTED');
CREATE TYPE customer_risk_level_enum AS ENUM ('LOW', 'MEDIUM', 'HIGH');
CREATE TYPE account_type_enum AS ENUM ('CHECKING', 'SAVINGS', 'LOAN', 'MERCHANT');
CREATE TYPE account_status_enum AS ENUM ('ACTIVE', 'DORMANT', 'FROZEN', 'CLOSED');
CREATE TYPE transaction_type_enum AS ENUM ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 'PAYMENT', 'FEE');
CREATE TYPE transaction_status_enum AS ENUM ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REVERSED');
CREATE TYPE ledger_entry_type_enum AS ENUM ('DEBIT', 'CREDIT');

-- ------------------------------------------
-- Trigger Function for updated_at
-- ------------------------------------------
-- This function automatically updates the 'updated_at' timestamp column
-- whenever a row in a table using it is updated.

CREATE
OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at
= NOW();
RETURN NEW;
END;
$$
LANGUAGE plpgsql;

-- ------------------------------------------
-- Table Definitions
-- ------------------------------------------

-- Represents individual or business customers
CREATE TABLE customers
(
    customer_id  UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
    first_name   VARCHAR(100),
    last_name    VARCHAR(100),
    email        VARCHAR(255) UNIQUE      NOT NULL,
    phone_number VARCHAR(50),
    address      TEXT,
    kyc_status   customer_kyc_status_enum NOT NULL,
    risk_level   customer_risk_level_enum NOT NULL,
    created_at   TIMESTAMPTZ              NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ              NOT NULL DEFAULT NOW()
);

-- Trigger for customers table
CREATE TRIGGER set_customer_timestamp
    BEFORE UPDATE
    ON customers
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();


-- Represents financial accounts held by customers
CREATE TABLE accounts
(
    account_id     UUID PRIMARY KEY             DEFAULT gen_random_uuid(),
    customer_id    UUID                NOT NULL REFERENCES customers (customer_id),
    account_number VARCHAR(50) UNIQUE  NOT NULL,
    account_type   account_type_enum   NOT NULL,
    currency       CHAR(3)             NOT NULL,                                   -- ISO 4217 currency code (e.g., 'USD', 'EUR')
    balance        DECIMAL(19, 4)      NOT NULL DEFAULT 0.00 CHECK (balance >= 0), -- Added non-negative check
    status         account_status_enum NOT NULL,
    created_at     TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ         NOT NULL DEFAULT NOW()
);

-- Trigger for accounts table
CREATE TRIGGER set_account_timestamp
    BEFORE UPDATE
    ON accounts
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();


-- Represents financial transactions between accounts or external entities
CREATE TABLE transactions
(
    transaction_id         UUID PRIMARY KEY                 DEFAULT gen_random_uuid(),
    source_account_id      UUID REFERENCES accounts (account_id),
    destination_account_id UUID REFERENCES accounts (account_id),
    transaction_type       transaction_type_enum   NOT NULL,
    amount                 DECIMAL(19, 4)          NOT NULL CHECK (amount > 0),
    currency               CHAR(3)                 NOT NULL,
    description            TEXT,
    status                 transaction_status_enum NOT NULL,
    failure_reason         TEXT,
    fraud_score            SMALLINT CHECK (fraud_score IS NULL OR (fraud_score >= 0 AND fraud_score <= 100)),
    initiated_at           TIMESTAMPTZ             NOT NULL DEFAULT NOW(),
    completed_at           TIMESTAMPTZ,         -- Timestamp when transaction reached a final state
    idempotency_key        VARCHAR(100) UNIQUE, -- Key to prevent duplicate processing
    -- Constraint to ensure at least one account is involved
    CONSTRAINT chk_transaction_accounts CHECK (source_account_id IS NOT NULL OR destination_account_id IS NOT NULL)
);
-- Note: No updated_at trigger for transactions as its state changes are typically tracked
-- via the 'status' field and 'completed_at' timestamp. Ledger entries are immutable.


-- Represents individual debit/credit entries for double-entry bookkeeping
CREATE TABLE ledger_entries
(
    entry_id        BIGSERIAL PRIMARY KEY,                        -- Auto-incrementing ID, could use UUIDv7
    transaction_id  UUID                   NOT NULL REFERENCES transactions (transaction_id),
    account_id      UUID                   NOT NULL REFERENCES accounts (account_id),
    entry_type      ledger_entry_type_enum NOT NULL,
    amount          DECIMAL(19, 4)         NOT NULL CHECK (amount > 0),
    currency        CHAR(3)                NOT NULL,
    entry_timestamp TIMESTAMPTZ            NOT NULL DEFAULT NOW() -- Timestamp of the ledger posting
);
-- Note: Ledger entries are typically immutable once created, so no updated_at.


-- ------------------------------------------
-- Indexes for Performance
-- ------------------------------------------
-- Basic indexes are crucial for query performance. More specific or composite
-- indexes might be needed based on actual query patterns in production.

CREATE INDEX idx_accounts_customer_id ON accounts (customer_id);
CREATE INDEX idx_accounts_account_number ON accounts (account_number);
CREATE INDEX idx_accounts_status ON accounts (status); -- Index status for filtering

CREATE INDEX idx_transactions_source_account_id ON transactions (source_account_id);
CREATE INDEX idx_transactions_destination_account_id ON transactions (destination_account_id);
CREATE INDEX idx_transactions_status ON transactions (status); -- Crucial for finding pending/failed
CREATE INDEX idx_transactions_initiated_at ON transactions (initiated_at); -- For time-based queries
CREATE INDEX idx_transactions_type ON transactions (transaction_type); -- If filtering by type often

CREATE INDEX idx_ledger_entries_transaction_id ON ledger_entries (transaction_id);
CREATE INDEX idx_ledger_entries_account_id ON ledger_entries (account_id);
CREATE INDEX idx_ledger_entries_entry_timestamp ON ledger_entries (entry_timestamp);
-- Often useful for fetching account history:
CREATE INDEX idx_ledger_entries_account_timestamp ON ledger_entries (account_id, entry_timestamp);



-- =============================================================================
-- Script to Create PostgreSQL Roles and Permissions for Fintech Microservices
-- =============================================================================
-- Database Schema Target: Assumes tables/types from 'fintech_schema_revised.sql'
--                         reside in the 'public' schema.
-- Date Created: 2025-04-12 16:57:07 UTC (Based on user request time)
-- Requesting User: ngnhng
-- =============================================================================

-- |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
-- IMPORTANT: Replace placeholder passwords with strong, unique passwords!
-- |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||

-- ------------------------------------------
-- Create Roles (Users) for Each Service
-- ------------------------------------------
-- Using DO blocks for basic idempotency (won't error if role exists)

DO
$$
BEGIN
   IF
NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'customer_service_role') THEN
CREATE ROLE customer_service_role LOGIN PASSWORD 'replace_with_strong_password_cust';
RAISE
NOTICE 'Role customer_service_role created.';
ELSE
      RAISE NOTICE 'Role customer_service_role already exists.';
END IF;
END
$$;

DO
$$
BEGIN
   IF
NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'account_service_role') THEN
CREATE ROLE account_service_role LOGIN PASSWORD 'replace_with_strong_password_acct';
RAISE
NOTICE 'Role account_service_role created.';
ELSE
      RAISE NOTICE 'Role account_service_role already exists.';
END IF;
END
$$;

DO
$$
BEGIN
   IF
NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'transaction_service_role') THEN
CREATE ROLE transaction_service_role LOGIN PASSWORD 'replace_with_strong_password_txn';
RAISE
NOTICE 'Role transaction_service_role created.';
ELSE
      RAISE NOTICE 'Role transaction_service_role already exists.';
END IF;
END
$$;

-- ------------------------------------------
-- Grant Schema Usage (Adjust 'public' if using dedicated schemas)
-- ------------------------------------------

GRANT USAGE ON SCHEMA
public TO customer_service_role;
GRANT USAGE ON SCHEMA
public TO account_service_role;
GRANT USAGE ON SCHEMA
public TO transaction_service_role;

-- ------------------------------------------
-- Grant Permissions for customer_service_role
-- ------------------------------------------
-- Manages the 'customers' table

-- Table Permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE customers TO customer_service_role;

-- Type Permissions (for ENUMs used in the customers table)
GRANT USAGE ON TYPE customer_kyc_status_enum TO customer_service_role;
GRANT USAGE ON TYPE customer_risk_level_enum TO customer_service_role;

-- Sequence Permissions (Not needed for UUID PKs)
-- GRANT USAGE, SELECT ON SEQUENCE customers_customer_id_seq TO customer_service_role; -- Example if using SERIAL


-- ------------------------------------------
-- Grant Permissions for account_service_role
-- ------------------------------------------
-- Manages the 'accounts' table, needs limited read access to 'customers'

-- Table Permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE accounts TO account_service_role;
GRANT SELECT (customer_id, kyc_status, risk_level) ON TABLE customers TO account_service_role;
-- Read specific customer details for validation

-- Type Permissions (for ENUMs used in accounts and relevant customer columns)
GRANT USAGE ON TYPE account_type_enum TO account_service_role;
GRANT USAGE ON TYPE account_status_enum TO account_service_role;
GRANT USAGE ON TYPE customer_kyc_status_enum TO account_service_role; -- Needed if reading kyc_status from customers
GRANT USAGE ON TYPE customer_risk_level_enum TO account_service_role;
-- Needed if reading risk_level from customers

-- Sequence Permissions (Not needed for UUID PKs)
-- GRANT USAGE, SELECT ON SEQUENCE accounts_account_id_seq TO account_service_role; -- Example if using SERIAL


-- ------------------------------------------
-- Grant Permissions for transaction_service_role
-- ------------------------------------------
-- Manages 'transactions' and 'ledger_entries', needs read access to 'accounts'

-- Table Permissions
GRANT SELECT, INSERT, UPDATE ON TABLE transactions TO transaction_service_role; -- UPDATE needed for status, completion time, etc.
GRANT SELECT, INSERT ON TABLE ledger_entries TO transaction_service_role;
GRANT SELECT (account_id, balance, status, currency) ON TABLE accounts TO transaction_service_role;
-- Read account details for validation/processing

-- IMPORTANT: Explicitly DO NOT grant UPDATE on accounts table, especially the balance column,
-- to enforce that balance changes go through the Account Service API/logic.

-- Type Permissions (for ENUMs used in transactions, ledger, and relevant account columns)
GRANT USAGE ON TYPE transaction_type_enum TO transaction_service_role;
GRANT USAGE ON TYPE transaction_status_enum TO transaction_service_role;
GRANT USAGE ON TYPE ledger_entry_type_enum TO transaction_service_role;
GRANT USAGE ON TYPE account_status_enum TO transaction_service_role;
-- Needed if reading status from accounts

-- Sequence Permissions (Needed for ledger_entries PK)
GRANT USAGE, SELECT ON SEQUENCE ledger_entries_entry_id_seq TO transaction_service_role;


-- =============================================================================
-- Granting Complete
-- =============================================================================
-- Remember to use these specific roles and their secure passwords in the
-- connection strings/configuration for each respective microservice.
-- =============================================================================
