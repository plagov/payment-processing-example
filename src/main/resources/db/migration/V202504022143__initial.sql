CREATE TABLE IF NOT EXISTS payments
(
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type                VARCHAR(255) NOT NULL,
    amount              NUMERIC(19, 4) NOT NULL,
    currency            VARCHAR(3) NOT NULL,
    debtor_iban         VARCHAR(34) NOT NULL,
    creditor_iban       VARCHAR(34) NOT NULL,
    details             VARCHAR(20) NOT NULL,
    status              VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    cancelled_at        TIMESTAMP WITH TIME ZONE NULL,
    cancellation_fee    NUMERIC(19, 4) NULL
);
