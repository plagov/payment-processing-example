package io.plagov.payment_processing.controller;

import io.plagov.payment_processing.models.PaymentRequest;
import io.plagov.payment_processing.models.PaymentResponse;
import io.plagov.payment_processing.service.PaymentProcessing;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PaymentsController {

    private final PaymentProcessing paymentProcessing;

    public PaymentsController(PaymentProcessing paymentProcessing) {
        this.paymentProcessing = paymentProcessing;
    }

    @PostMapping("/payments")
    public ResponseEntity<PaymentResponse> savePayment(@Valid @RequestBody PaymentRequest request) {
        var response = paymentProcessing.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
