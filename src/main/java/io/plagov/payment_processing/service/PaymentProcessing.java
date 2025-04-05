package io.plagov.payment_processing.service;

import io.plagov.payment_processing.models.PaymentFullResponse;
import io.plagov.payment_processing.models.PaymentRequest;
import io.plagov.payment_processing.models.PaymentResponse;
import io.plagov.payment_processing.models.enums.PaymentStatus;

import java.util.List;
import java.util.UUID;

public interface PaymentProcessing {

    PaymentFullResponse create(PaymentRequest paymentRequest);

    PaymentFullResponse cancel(UUID paymentId);

    List<PaymentResponse> queryPayments(PaymentStatus status,
                                        Double isEqualTo,
                                        Double isGreaterThan,
                                        Double isLessThan);
}
