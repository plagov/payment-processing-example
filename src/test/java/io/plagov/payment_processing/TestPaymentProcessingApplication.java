package io.plagov.payment_processing;

import io.plagov.payment_processing.configuration.ContainersConfig;
import org.springframework.boot.SpringApplication;

public class TestPaymentProcessingApplication {

    public static void main(String[] args) {
        SpringApplication.from(PaymentProcessingApplication::main).with(ContainersConfig.class).run(args);
    }

}
