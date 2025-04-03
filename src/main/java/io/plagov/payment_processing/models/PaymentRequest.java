package io.plagov.payment_processing.models;

import org.joda.money.Money;

public record PaymentRequest(
        Money amount,
        String debtorIban,
        String creditorIban,
        String details
) { }
