package io.plagov.payment_processing.mapper;

import io.plagov.payment_processing.entities.PaymentEntity;
import io.plagov.payment_processing.models.PaymentRequest;
import io.plagov.payment_processing.models.PaymentResponse;
import io.plagov.payment_processing.models.enums.PaymentStatus;
import io.plagov.payment_processing.models.enums.PaymentType;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class PaymentMapper {

    public PaymentEntity toPaymentEntity(PaymentRequest paymentRequest) {
        var now = Instant.now();
        return PaymentEntity.builder()
                .type(PaymentType.PESA)
                .amount(paymentRequest.amount())
                .currency(paymentRequest.currency())
                .debtorIban(paymentRequest.debtorIban())
                .creditorIban(paymentRequest.creditorIban())
                .details(paymentRequest.details())
                .status(PaymentStatus.ACCEPTED)
                .createdAt(now)
                .build();
    }

    public PaymentResponse toPaymentResponse(PaymentEntity entity) {
        return new PaymentResponse(
                entity.getId(),
                entity.getType(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getDebtorIban(),
                entity.getCreditorIban(),
                entity.getDetails(),
                entity.getStatus(),
                entity.getCreatedAt(),
                Optional.ofNullable(entity.getCancelledAt()),
                Optional.ofNullable(entity.getCancellationFee()));
    }
}
