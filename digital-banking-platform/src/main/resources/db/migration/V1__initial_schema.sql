-- CloudBank schema for Azure SQL Database (SQL Server)

CREATE TABLE customers (
    id              UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    created_at      DATETIME2        NOT NULL,
    updated_at      DATETIME2        NOT NULL,
    version         BIGINT           NOT NULL DEFAULT 0,
    customer_number NVARCHAR(20)     NOT NULL,
    first_name      NVARCHAR(100)    NOT NULL,
    last_name       NVARCHAR(100)    NOT NULL,
    email           NVARCHAR(255)    NOT NULL,
    phone_number    NVARCHAR(20)     NOT NULL,
    date_of_birth   DATE             NOT NULL,
    address_line1   NVARCHAR(255)    NOT NULL,
    city            NVARCHAR(100)    NOT NULL,
    province        NVARCHAR(100)    NOT NULL,
    postal_code     NVARCHAR(20)     NOT NULL,
    country         NVARCHAR(100)    NOT NULL,
    status          NVARCHAR(20)     NOT NULL,
    CONSTRAINT pk_customers PRIMARY KEY (id),
    CONSTRAINT uq_customers_customer_number UNIQUE (customer_number),
    CONSTRAINT uq_customers_email UNIQUE (email)
);

CREATE TABLE accounts (
    id                   UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    created_at           DATETIME2        NOT NULL,
    updated_at           DATETIME2        NOT NULL,
    version              BIGINT           NOT NULL DEFAULT 0,
    account_number       NVARCHAR(20)     NOT NULL,
    customer_id          UNIQUEIDENTIFIER NOT NULL,
    account_type         NVARCHAR(20)     NOT NULL,
    account_status       NVARCHAR(20)     NOT NULL,
    currency             NVARCHAR(3)      NOT NULL,
    current_balance      DECIMAL(19, 2)   NOT NULL,
    available_balance    DECIMAL(19, 2)   NOT NULL,
    daily_transfer_limit DECIMAL(19, 2)   NOT NULL,
    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT uq_accounts_account_number UNIQUE (account_number),
    CONSTRAINT fk_accounts_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT ck_accounts_balances CHECK (current_balance >= 0 AND available_balance >= 0)
);

CREATE INDEX idx_accounts_customer_id ON accounts (customer_id);
CREATE INDEX idx_accounts_account_number ON accounts (account_number);

CREATE TABLE beneficiaries (
    id                         UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    created_at                 DATETIME2        NOT NULL,
    updated_at                 DATETIME2        NOT NULL,
    version                    BIGINT           NOT NULL DEFAULT 0,
    customer_id                UNIQUEIDENTIFIER NOT NULL,
    beneficiary_name           NVARCHAR(150)    NOT NULL,
    beneficiary_account_number NVARCHAR(20)     NOT NULL,
    bank_name                  NVARCHAR(150)    NOT NULL,
    transit_number             NVARCHAR(5)      NOT NULL,
    institution_number         NVARCHAR(3)      NOT NULL,
    nickname                   NVARCHAR(50)     NULL,
    status                     NVARCHAR(20)     NOT NULL,
    CONSTRAINT pk_beneficiaries PRIMARY KEY (id),
    CONSTRAINT fk_beneficiaries_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT uq_beneficiaries_customer_account UNIQUE (customer_id, beneficiary_account_number)
);

CREATE TABLE payments (
    id                         UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    created_at                 DATETIME2        NOT NULL,
    updated_at                 DATETIME2        NOT NULL,
    version                    BIGINT           NOT NULL DEFAULT 0,
    payment_reference          NVARCHAR(20)     NOT NULL,
    source_account_id          UNIQUEIDENTIFIER NOT NULL,
    beneficiary_id             UNIQUEIDENTIFIER NOT NULL,
    destination_account_number NVARCHAR(20)     NOT NULL,
    amount                     DECIMAL(19, 2)   NOT NULL,
    currency                   NVARCHAR(3)      NOT NULL,
    description                NVARCHAR(500)    NULL,
    idempotency_key            NVARCHAR(100)    NOT NULL,
    status                     NVARCHAR(20)     NOT NULL,
    failure_reason             NVARCHAR(500)    NULL,
    completed_at               DATETIME2        NULL,
    CONSTRAINT pk_payments PRIMARY KEY (id),
    CONSTRAINT uq_payments_payment_reference UNIQUE (payment_reference),
    CONSTRAINT uq_payments_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT fk_payments_source_account FOREIGN KEY (source_account_id) REFERENCES accounts (id),
    CONSTRAINT fk_payments_beneficiary FOREIGN KEY (beneficiary_id) REFERENCES beneficiaries (id)
);

CREATE INDEX idx_payments_idempotency_key ON payments (idempotency_key);
CREATE INDEX idx_payments_source_account_id ON payments (source_account_id);
CREATE INDEX idx_payments_created_at ON payments (created_at);

CREATE TABLE transactions (
    id                     UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    created_at             DATETIME2        NOT NULL,
    updated_at             DATETIME2        NOT NULL,
    version                BIGINT           NOT NULL DEFAULT 0,
    transaction_reference  NVARCHAR(20)     NOT NULL,
    account_id             UNIQUEIDENTIFIER NOT NULL,
    related_account_id     UNIQUEIDENTIFIER NULL,
    payment_id             UNIQUEIDENTIFIER NULL,
    transaction_type       NVARCHAR(20)     NOT NULL,
    amount                 DECIMAL(19, 2)   NOT NULL,
    currency               NVARCHAR(3)      NOT NULL,
    description            NVARCHAR(500)    NULL,
    status                 NVARCHAR(20)     NOT NULL,
    balance_before         DECIMAL(19, 2)   NOT NULL,
    balance_after          DECIMAL(19, 2)   NOT NULL,
    CONSTRAINT pk_transactions PRIMARY KEY (id),
    CONSTRAINT uq_transactions_transaction_reference UNIQUE (transaction_reference),
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT fk_transactions_payment FOREIGN KEY (payment_id) REFERENCES payments (id)
);

CREATE INDEX idx_transactions_account_id ON transactions (account_id);
CREATE INDEX idx_transactions_payment_id ON transactions (payment_id);
CREATE INDEX idx_transactions_created_at ON transactions (created_at);

CREATE TABLE notifications (
    id          UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    created_at  DATETIME2        NOT NULL,
    updated_at  DATETIME2        NOT NULL,
    version     BIGINT           NOT NULL DEFAULT 0,
    customer_id UNIQUEIDENTIFIER NOT NULL,
    type        NVARCHAR(30)     NOT NULL,
    message     NVARCHAR(1000)   NOT NULL,
    status      NVARCHAR(20)     NOT NULL,
    is_read     BIT              NOT NULL DEFAULT 0,
    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT fk_notifications_customer FOREIGN KEY (customer_id) REFERENCES customers (id)
);

CREATE TABLE audit_events (
    id               UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    created_at       DATETIME2        NOT NULL,
    updated_at       DATETIME2        NOT NULL,
    version          BIGINT           NOT NULL DEFAULT 0,
    event_reference  NVARCHAR(20)     NOT NULL,
    actor_id         NVARCHAR(50)     NOT NULL,
    action           NVARCHAR(40)     NOT NULL,
    resource_type    NVARCHAR(30)     NOT NULL,
    resource_id      NVARCHAR(50)     NOT NULL,
    description      NVARCHAR(1000)   NOT NULL,
    result           NVARCHAR(20)     NOT NULL,
    correlation_id   NVARCHAR(50)     NOT NULL,
    CONSTRAINT pk_audit_events PRIMARY KEY (id),
    CONSTRAINT uq_audit_events_event_reference UNIQUE (event_reference)
);
