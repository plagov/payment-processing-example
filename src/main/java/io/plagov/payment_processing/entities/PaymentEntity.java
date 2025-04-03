package io.plagov.payment_processing.entities;

import io.plagov.payment_processing.models.enums.PaymentStatus;
import io.plagov.payment_processing.models.enums.PaymentType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
@Data
public class PaymentEntity {
    private UUID id;
    private PaymentType type;
    private BigDecimal amount;
    private String currency;
    private String debtorIban;
    private String creditorIban;
    private String details;
    private PaymentStatus status;
    private Instant createdAt;
    private Instant cancelledAt;
    private BigDecimal cancellationFee;
}
