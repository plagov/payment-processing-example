package io.plagov.payment_processing.models;

import io.plagov.payment_processing.annotations.PositiveAmount;
import io.plagov.payment_processing.annotations.ValidCurrency;
import io.plagov.payment_processing.annotations.ValidIban;
import jakarta.validation.constraints.Size;
import org.joda.money.Money;

public record PaymentRequest(
        @PositiveAmount @ValidCurrency Money amount,
        @ValidIban String debtorIban,
        @ValidIban String creditorIban,
        @Size(max = 200) String details
) { }
