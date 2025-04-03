package io.plagov.payment_processing.models;

import java.util.Currency;

public record PaymentRequest(
        double amount,
        String currency,
        String debtorIban,
        String creditorIban,
        String details
) { }
