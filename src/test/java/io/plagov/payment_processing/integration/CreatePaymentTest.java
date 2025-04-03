package io.plagov.payment_processing.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.plagov.payment_processing.configuration.ContainersConfig;
import io.plagov.payment_processing.models.PaymentRequest;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Testcontainers
@SpringBootTest
@Import(ContainersConfig.class)
@AutoConfigureMockMvc
class CreatePaymentTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateNewPayment() throws Exception {
        var paymentRequest = new PaymentRequest(Money.parse("EUR 150.25"),
                "EE123",
                "LT456",
                "test details");

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.type").value("PESA"))
                .andExpect(jsonPath("$.amount").value("EUR 150.25"))
                .andExpect(jsonPath("$.debtorIban").value("EE123"))
                .andExpect(jsonPath("$.creditorIban").value("LT456"))
                .andExpect(jsonPath("$.details").value("test details"))
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.cancelledAt").isEmpty())
                .andExpect(jsonPath("$.cancellationFee").isEmpty());
    }
}
