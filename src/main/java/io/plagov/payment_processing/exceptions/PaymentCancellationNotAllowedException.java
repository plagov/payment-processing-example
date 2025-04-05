package io.plagov.payment_processing.exceptions;

public class PaymentCancellationNotAllowedException extends RuntimeException {
    public PaymentCancellationNotAllowedException(String message) {
        super(message);
    }
}
