package io.plagov.payment_processing.mapper;

import io.plagov.payment_processing.entities.PaymentEntity;
import io.plagov.payment_processing.models.PaymentFullResponse;
import io.plagov.payment_processing.models.PaymentRequest;
import io.plagov.payment_processing.models.PaymentResponse;
import io.plagov.payment_processing.models.enums.PaymentStatus;
import io.plagov.payment_processing.models.enums.PaymentType;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.time.Instant;

@Component
public class PaymentMapper {

    public PaymentEntity toPaymentEntity(PaymentRequest paymentRequest, PaymentType paymentType) {
        var now = Instant.now();
        return PaymentEntity.builder()
                .type(paymentType)
                .amount(paymentRequest.amount().getAmount())
                .currency(paymentRequest.amount().getCurrencyUnit().getCode())
                .debtorIban(paymentRequest.debtorIban())
                .creditorIban(paymentRequest.creditorIban())
                .details(paymentRequest.details())
                .status(PaymentStatus.ACCEPTED)
                .createdAt(now)
                .build();
    }

    public PaymentFullResponse toPaymentFullResponse(PaymentEntity entity) {
        var currency = CurrencyUnit.of(entity.getCurrency());
        var amount = entity.getAmount().setScale(currency.getDecimalPlaces(), RoundingMode.HALF_UP);
        var money = Money.of(currency, amount);
        return new PaymentFullResponse(
                entity.getId(),
                entity.getType(),
                money,
                entity.getDebtorIban(),
                entity.getCreditorIban(),
                entity.getDetails(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getCancelledAt(),
                entity.getCancellationFee());
    }

    public PaymentResponse toPaymentResponse(PaymentEntity entity) {
        var currency = CurrencyUnit.of(entity.getCurrency());
        var amount = entity.getAmount().setScale(currency.getDecimalPlaces(), RoundingMode.HALF_UP);
        var money = Money.of(currency, amount);
        return new PaymentResponse(
                entity.getId(),
                money,
                entity.getDebtorIban(),
                entity.getCreditorIban(),
                entity.getDetails(),
                entity.getCreatedAt(),
                entity.getCancelledAt(),
                entity.getCancellationFee());
    }
}
