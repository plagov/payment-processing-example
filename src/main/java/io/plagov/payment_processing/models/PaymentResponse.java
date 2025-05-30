package io.plagov.payment_processing.models;

import org.joda.money.Money;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        Money amount,
        String debtorIban,
        String creditorIban,
        String details,
        Instant createdAt,
        Instant cancelledAt,
        BigDecimal cancellationFee
) { }
