package io.plagov.payment_processing.service;

import io.plagov.payment_processing.models.PaymentRequest;
import io.plagov.payment_processing.models.PaymentResponse;

public interface PaymentProcessing {

    PaymentResponse create(PaymentRequest paymentRequest);
}
