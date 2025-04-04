package io.plagov.payment_processing.service;

import io.plagov.payment_processing.dao.Dao;
import io.plagov.payment_processing.entities.PaymentEntity;
import io.plagov.payment_processing.mapper.PaymentMapper;
import io.plagov.payment_processing.models.PaymentRequest;
import io.plagov.payment_processing.models.PaymentResponse;
import io.plagov.payment_processing.models.enums.PaymentType;
import org.joda.money.CurrencyUnit;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Component
public class DefaultPaymentProcessing implements PaymentProcessing {

    private final Dao<PaymentEntity, UUID> paymentsDao;
    private final PaymentMapper paymentMapper;

    // we assume this rate was gotten from an external exchange-rate provider
    private final BigDecimal USD_TO_EUR_RATE = new BigDecimal("1.09");

    public DefaultPaymentProcessing(Dao<PaymentEntity, UUID> paymentsDao, PaymentMapper paymentMapper) {
        this.paymentsDao = paymentsDao;
        this.paymentMapper = paymentMapper;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        var paymentType = resolvePaymentType(paymentRequest);
        var paymentEntity = paymentMapper.toPaymentEntity(paymentRequest, paymentType);
        var paymentId = paymentsDao.save(paymentEntity);
        paymentEntity.setId(paymentId);
        return paymentMapper.toPaymentResponse(paymentEntity);
    }

    @Override
    public PaymentResponse cancel(UUID paymentId) {
        var paymentEntity = paymentsDao.getById(paymentId)
                .orElseThrow(() -> new NoSuchElementException("Payment not found"));
        
        resolveIfEligibleForCancellation(paymentEntity);

        var cancellationFee = calculateCancellationFee(paymentEntity);
        var cancellationTime = Instant.now();
        var canceledPaymentEntity = paymentsDao.cancel(paymentId, cancellationTime, cancellationFee);

        return paymentMapper.toPaymentResponse(canceledPaymentEntity);
    }

    private BigDecimal calculateCancellationFee(PaymentEntity paymentEntity) {
        var oneHourAgo = Instant.now().minus(Duration.ofHours(1));
        var fixedFee = new BigDecimal("0.05");

        BigDecimal amount = paymentEntity.getAmount();
        if (paymentEntity.getCurrency().equals(CurrencyUnit.USD.getCode())) {
            amount = amount.divide(USD_TO_EUR_RATE, 2, RoundingMode.HALF_UP);
        }

        if (paymentEntity.getCreatedAt().isAfter(oneHourAgo)) {
            return amount.multiply(BigDecimal.valueOf(0.01));
        } else {
            return amount.multiply(BigDecimal.valueOf(0.02)).add(fixedFee);
        }
    }

    private static void resolveIfEligibleForCancellation(PaymentEntity paymentEntity) {
        var today = Instant.now().truncatedTo(ChronoUnit.DAYS);
        if (!paymentEntity.getCreatedAt().truncatedTo(ChronoUnit.DAYS).equals(today)) {
            throw new IllegalArgumentException("Cannot cancel payment");
        }
    }

    private PaymentType resolvePaymentType(PaymentRequest paymentRequest) {
        var isEurPayment = paymentRequest.amount().getCurrencyUnit().equals(CurrencyUnit.EUR);
        var pesaCountries = List.of("EE", "LT", "LV");
        var debtorCountry = paymentRequest.debtorIban().substring(0, 2);
        var creditorCountry = paymentRequest.creditorIban().substring(0, 2);

        if (isEurPayment && pesaCountries.containsAll(List.of(debtorCountry, creditorCountry))) {
            return PaymentType.PESA;
        }

        return PaymentType.SWIFT;
    }
}
