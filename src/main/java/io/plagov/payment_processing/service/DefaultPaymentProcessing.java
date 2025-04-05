package io.plagov.payment_processing.service;

import io.plagov.payment_processing.dao.Dao;
import io.plagov.payment_processing.entities.PaymentEntity;
import io.plagov.payment_processing.exceptions.PaymentCancellationNotAllowedException;
import io.plagov.payment_processing.exceptions.PaymentNotFoundException;
import io.plagov.payment_processing.mapper.PaymentMapper;
import io.plagov.payment_processing.models.PaymentFullResponse;
import io.plagov.payment_processing.models.PaymentRequest;
import io.plagov.payment_processing.models.PaymentResponse;
import io.plagov.payment_processing.models.enums.PaymentStatus;
import io.plagov.payment_processing.models.enums.PaymentType;
import org.joda.money.CurrencyUnit;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Component
public class DefaultPaymentProcessing implements PaymentProcessing {

    private final Dao<PaymentEntity, UUID> paymentsDao;
    private final PaymentMapper paymentMapper;
    private final NotificationService notificationService;

    // we assume this rate was gotten from an external exchange-rate provider
    private final BigDecimal USD_TO_EUR_RATE = new BigDecimal("1.09");

    public DefaultPaymentProcessing(Dao<PaymentEntity, UUID> paymentsDao,
                                    PaymentMapper paymentMapper,
                                    NotificationService notificationService) {
        this.paymentsDao = paymentsDao;
        this.paymentMapper = paymentMapper;
        this.notificationService = notificationService;
    }

    @Override
    public PaymentFullResponse create(PaymentRequest paymentRequest) {
        var paymentType = resolvePaymentType(paymentRequest);
        var paymentEntity = paymentMapper.toPaymentEntity(paymentRequest, paymentType);
        var paymentId = paymentsDao.save(paymentEntity);
        paymentEntity.setId(paymentId);
        var paymentFullResponse = paymentMapper.toPaymentFullResponse(paymentEntity);
        notificationService.notifyAboutCreatedPayment(paymentFullResponse);
        return paymentFullResponse;
    }

    @Override
    public PaymentFullResponse cancel(UUID paymentId) {
        var paymentEntity = paymentsDao.getById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment with ID %s not found".formatted(paymentId)));

        resolveIfEligibleForCancellation(paymentEntity);

        var cancellationFee = calculateCancellationFee(paymentEntity);
        var cancellationTime = Instant.now();
        var canceledPaymentEntity = paymentsDao.cancel(paymentId, cancellationTime, cancellationFee);

        var paymentFullResponse = paymentMapper.toPaymentFullResponse(canceledPaymentEntity);
        notificationService.notifyAboutCancelledPayment(paymentFullResponse);
        return paymentFullResponse;
    }

    @Override
    public List<PaymentResponse> queryPayments(PaymentStatus status,
                                               Double isEqualTo,
                                               Double isGreaterThan,
                                               Double isLessThan) {
        var paymentEntities = paymentsDao.queryPayments(status, isEqualTo, isGreaterThan, isLessThan);
        return paymentEntities.stream().map(paymentMapper::toPaymentResponse).toList();
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
            throw new PaymentCancellationNotAllowedException("Payments older than today are not eligible for cancellation");
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
