package io.plagov.payment_processing;

import io.plagov.payment_processing.configuration.ContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(ContainersConfig.class)
@SpringBootTest
class PaymentProcessingApplicationTests {

    @Test
    void contextLoads() {
    }

}
