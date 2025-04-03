package io.plagov.payment_processing.models;

import io.plagov.payment_processing.models.enums.PaymentStatus;
import io.plagov.payment_processing.models.enums.PaymentType;

import java.time.Instant;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        PaymentType type,
        double amount,
        String currency,
        String debtorIban,
        String creditorIban,
        String details,
        PaymentStatus status,
        Instant createdAt,
        Optional<Instant> cancelledAt,
        Optional<Double> cancellationFee
) { }
