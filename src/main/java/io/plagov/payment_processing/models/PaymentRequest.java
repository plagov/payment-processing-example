package io.plagov.payment_processing.models;

import io.plagov.payment_processing.annotations.PositiveAmount;
import jakarta.validation.constraints.Size;
import org.joda.money.Money;

public record PaymentRequest(
        @PositiveAmount Money amount,
        String debtorIban,
        String creditorIban,
        @Size(max = 200) String details
) { }
