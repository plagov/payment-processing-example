package io.plagov.payment_processing;

import org.springframework.boot.SpringApplication;

public class TestPaymentProcessingApplication {

    public static void main(String[] args) {
        SpringApplication.from(PaymentProcessingApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
