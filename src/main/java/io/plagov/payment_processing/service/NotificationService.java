package io.plagov.payment_processing.service;

import io.plagov.payment_processing.models.PaymentFullResponse;

public interface NotificationService {

    void notifyAboutCreatedPayment(PaymentFullResponse response);
    void notifyAboutCancelledPayment(PaymentFullResponse response);
}
