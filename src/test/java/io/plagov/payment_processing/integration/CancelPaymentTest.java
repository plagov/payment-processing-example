package io.plagov.payment_processing.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.plagov.payment_processing.configuration.ContainersConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
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

    @Autowired
    private JdbcClient jdbcClient;

    @ParameterizedTest
    @MethodSource("currencyWithFeeWithinOneHour")
    void shouldCancelPaymentWithinOneHour(String currency, String fee) throws Exception {
        var createdAt = Instant.now().minus(Duration.ofMinutes(5));
        var paymentId = addPayment(currency, createdAt);

        mockMvc.perform(
                post("/api/v1/payments/%s/cancel".formatted(paymentId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancelledAt").exists())
                .andExpect(jsonPath("$.cancellationFee").value(fee));
    }

    @ParameterizedTest
    @MethodSource("currencyWithFeeWithinSameDay")
    void shouldCancelPaymentWithinSameDay(String currency, String fee) throws Exception {
        var createdAt = Instant.now().minus(Duration.ofMinutes(65));
        var uuid = addPayment(currency, createdAt);
        mockMvc.perform(
                        post("/api/v1/payments/%s/cancel".formatted(uuid))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancelledAt").exists())
                .andExpect(jsonPath("$.cancellationFee").value(fee));
    }

    @Test
    void shouldNotCancelPaymentOlderThanOneDay() throws Exception {
        var createdAt = Instant.now().minus(Duration.ofDays(1));
        var uuid = addPayment("EUR", createdAt);
        mockMvc.perform(
                        post("/api/v1/payments/%s/cancel".formatted(uuid))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("Payments older than today are not eligible for cancellation"));
    }

    private static Stream<Arguments> currencyWithFeeWithinOneHour() {
        return Stream.of(
                Arguments.of("EUR", "1.0"),
                Arguments.of("USD", "0.9174")
        );
    }

    private static Stream<Arguments> currencyWithFeeWithinSameDay() {
        return Stream.of(
                Arguments.of("EUR", "2.05"),
                Arguments.of("USD", "1.8848")
        );
    }

    private UUID addPayment(String currency, Instant createdAt) {
        var query = """
            INSERT INTO payments (type, amount, currency, debtor_iban, creditor_iban, details, status, created_at)
            VALUES (:type, :amount, :currency, :debtor_iban, :creditor_iban, :details, :status, :created_at)
            RETURNING id""";

        var paymentId = jdbcClient.sql(query)
                .param("type", "PESA")
                .param("amount", new BigDecimal("100.00"))
                .param("currency", currency)
                .param("debtor_iban", "EE382200221020145685")
                .param("creditor_iban", "LT121000011101001000")
                .param("details", "detail")
                .param("status", "ACCEPTED")
                .param("created_at", Timestamp.from(createdAt))
                .query(String.class)
                .single();

        return UUID.fromString(paymentId);
    }
}
