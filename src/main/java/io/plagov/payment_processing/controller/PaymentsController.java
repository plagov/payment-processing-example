package io.plagov.payment_processing.controller;

import io.plagov.payment_processing.models.PaymentRequest;
import io.plagov.payment_processing.models.PaymentResponse;
import io.plagov.payment_processing.models.enums.PaymentStatus;
import io.plagov.payment_processing.service.PaymentProcessing;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

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

    @PostMapping("/payments/{paymentId}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable UUID paymentId) {
        var response = paymentProcessing.cancel(paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/payments")
    public ResponseEntity<List<PaymentResponse>> queryPayments(
            @RequestParam PaymentStatus status,
            @RequestParam(required = false) Double isEqualTo,
            @RequestParam(required = false) Double isGreaterThan,
            @RequestParam(required = false) Double isLessThan) {
        var payments = paymentProcessing.queryPayments(status, isEqualTo, isGreaterThan, isLessThan);
        return ResponseEntity.ok(payments);
    }
}
