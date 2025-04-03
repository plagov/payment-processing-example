package io.plagov.payment_processing.models;

import io.plagov.payment_processing.models.enums.PaymentStatus;
import io.plagov.payment_processing.models.enums.PaymentType;
import org.joda.money.Money;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        PaymentType type,
        Money amount,
        String debtorIban,
        String creditorIban,
        String details,
        PaymentStatus status,
        Instant createdAt,
        Optional<Instant> cancelledAt,
        Optional<BigDecimal> cancellationFee
) { }
