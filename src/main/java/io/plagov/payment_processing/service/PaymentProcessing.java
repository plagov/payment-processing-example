package io.plagov.payment_processing.service;

import io.plagov.payment_processing.models.PaymentRequest;
import io.plagov.payment_processing.models.PaymentResponse;

import java.util.UUID;

public interface PaymentProcessing {

    PaymentResponse create(PaymentRequest paymentRequest);

    PaymentResponse cancel(UUID paymentId);
}
