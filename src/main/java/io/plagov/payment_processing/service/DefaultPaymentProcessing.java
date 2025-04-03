package io.plagov.payment_processing.service;

import io.plagov.payment_processing.dao.Dao;
import io.plagov.payment_processing.entities.PaymentEntity;
import io.plagov.payment_processing.mapper.PaymentMapper;
import io.plagov.payment_processing.models.PaymentRequest;
import io.plagov.payment_processing.models.PaymentResponse;
import io.plagov.payment_processing.models.enums.PaymentType;
import org.joda.money.CurrencyUnit;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class DefaultPaymentProcessing implements PaymentProcessing {

    private final Dao<PaymentEntity, UUID> paymentsDao;
    private final PaymentMapper paymentMapper;

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
