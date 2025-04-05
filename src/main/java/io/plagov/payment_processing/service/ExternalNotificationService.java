package io.plagov.payment_processing.service;

import io.plagov.payment_processing.models.PaymentFullResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class ExternalNotificationService implements NotificationService{

    private static final Logger LOGGER = LogManager.getLogger(ExternalNotificationService.class.getName());

    @Override
    public void notifyAboutCreatedPayment(PaymentFullResponse response) {
        LOGGER.info("Event: payment creation. Payment ID: {}", response.id());
    }

    @Override
    public void notifyAboutCancelledPayment(PaymentFullResponse response) {
        LOGGER.info("Event: payment cancellation. Payment ID: {}", response.id());
    }
}
