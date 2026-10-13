CREATE TABLE bank_user (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password_hash   VARCHAR(100) NOT NULL,
    full_name       VARCHAR(120) NOT NULL,
    email           VARCHAR(150) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE account (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES bank_user(id),
    account_number  VARCHAR(20) NOT NULL UNIQUE,
    account_type    VARCHAR(20) NOT NULL,
    nickname        VARCHAR(80) NOT NULL,
    balance         NUMERIC(14,2) NOT NULL DEFAULT 0,
    currency        VARCHAR(3) NOT NULL DEFAULT 'LKR',
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE transaction (
    id                  BIGSERIAL PRIMARY KEY,
    account_id          BIGINT NOT NULL REFERENCES account(id),
    type                VARCHAR(30) NOT NULL,
    amount              NUMERIC(14,2) NOT NULL,
    description         VARCHAR(255) NOT NULL,
    counterparty        VARCHAR(120),
    status              VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    created_at          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE card (
    id              BIGSERIAL PRIMARY KEY,
    account_id      BIGINT NOT NULL REFERENCES account(id),
    card_number     VARCHAR(25) NOT NULL,
    card_type       VARCHAR(20) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expiry_month    INT NOT NULL,
    expiry_year     INT NOT NULL,
    spending_limit  NUMERIC(14,2),
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE loan (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES bank_user(id),
    account_id          BIGINT NOT NULL REFERENCES account(id),
    loan_type           VARCHAR(30) NOT NULL,
    principal           NUMERIC(14,2) NOT NULL,
    interest_rate       NUMERIC(5,2) NOT NULL,
    term_months         INT NOT NULL,
    outstanding_balance NUMERIC(14,2) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE biller (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    category    VARCHAR(50) NOT NULL
);

CREATE TABLE bill_payment (
    id          BIGSERIAL PRIMARY KEY,
    account_id  BIGINT NOT NULL REFERENCES account(id),
    biller_id   BIGINT NOT NULL REFERENCES biller(id),
    amount      NUMERIC(14,2) NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'PAID',
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE notification (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES bank_user(id),
    title       VARCHAR(150) NOT NULL,
    message     VARCHAR(500) NOT NULL,
    event_type  VARCHAR(50) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO biller (name, category) VALUES
    ('Ceylon Electricity Board', 'Electricity'),
    ('National Water Supply', 'Water'),
    ('SLT Mobitel', 'Internet & Mobile'),
    ('Imagine Lanka Credit Card', 'Credit Card'),
    ('Dialog Axiata', 'Mobile');
