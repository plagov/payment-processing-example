package io.plagov.payment_processing.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.plagov.payment_processing.configuration.ContainersConfig;
import io.plagov.payment_processing.models.PaymentResponse;
import org.joda.money.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@Import(ContainersConfig.class)
@AutoConfigureMockMvc
class CancelPaymentTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @MethodSource("amountWithFeeSource")
    void shouldCancelPaymentWithinOneHour(String amount, String fee) throws Exception {
        var paymentRequest = Map.of("amount", Money.parse(amount),
                "debtorIban", "EE382200221020145685",
                "creditorIban", "LT121000011101001000",
                "details", "test details");

        var mvcResult = mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andReturn();
        var response = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(), PaymentResponse.class);

        mockMvc.perform(
                post("/api/v1/payments/%s/cancel".formatted(response.id()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancelledAt").exists())
                .andExpect(jsonPath("$.cancellationFee").value(fee));
    }

    private static Stream<Arguments> amountWithFeeSource() {
        return Stream.of(
                Arguments.of("EUR 100.00", "1.0"),
                Arguments.of("USD 100.00", "0.9174")
        );
    }
}
