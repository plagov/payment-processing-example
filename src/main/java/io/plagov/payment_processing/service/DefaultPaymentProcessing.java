package io.plagov.payment_processing.service;

import io.plagov.payment_processing.dao.Dao;
import io.plagov.payment_processing.entities.PaymentEntity;
import io.plagov.payment_processing.mapper.PaymentMapper;
import io.plagov.payment_processing.models.PaymentRequest;
import io.plagov.payment_processing.models.PaymentResponse;
import org.springframework.stereotype.Component;

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
        var paymentEntity = paymentMapper.toPaymentEntity(paymentRequest);
        var paymentId = paymentsDao.save(paymentEntity);
        paymentEntity.setId(paymentId);
        return paymentMapper.toPaymentResponse(paymentEntity);
    }
}
