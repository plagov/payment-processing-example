package io.plagov.payment_processing.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.plagov.payment_processing.configuration.ContainersConfig;
import io.plagov.payment_processing.models.PaymentResponse;
import org.joda.money.Money;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
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
    private JdbcTemplate jdbcTemplate;

    @ParameterizedTest
    @MethodSource("amountWithFeeWithinOneHour")
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

    @ParameterizedTest
    @MethodSource("currencyWithFeeWithinSameDay")
    void shouldCancelPaymentWithinSameDay(String currency, String fee) throws Exception {
        var uuid = addSameDayPayment(currency);
        mockMvc.perform(
                        post("/api/v1/payments/%s/cancel".formatted(uuid))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancelledAt").exists())
                .andExpect(jsonPath("$.cancellationFee").value(fee));
    }

    private static Stream<Arguments> amountWithFeeWithinOneHour() {
        return Stream.of(
                Arguments.of("EUR 100.00", "1.0"),
                Arguments.of("USD 100.00", "0.9174")
        );
    }

    private static Stream<Arguments> currencyWithFeeWithinSameDay() {
        return Stream.of(
                Arguments.of("EUR", "2.05"),
                Arguments.of("USD", "1.8848")
        );
    }

    private UUID addSameDayPayment(String currency) {
        var createdAt = Instant.now().minus(Duration.ofMinutes(65));
        String query = """
            INSERT INTO payments (type, amount, currency, debtor_iban, creditor_iban, details, status, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)""";

        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(query, new String[]{"id"});
            ps.setString(1, "PESA");
            ps.setBigDecimal(2, new BigDecimal("100.00"));
            ps.setString(3, currency);
            ps.setString(4, "EE382200221020145685");
            ps.setString(5, "LT121000011101001000");
            ps.setString(6, "detail");
            ps.setString(7, "ACCEPTED");
            ps.setTimestamp(8, Timestamp.from(createdAt));
            return ps;
        }, keyHolder);

        return UUID.fromString(keyHolder.getKeys().get("id").toString());
    }
}
