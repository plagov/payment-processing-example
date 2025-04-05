package io.plagov.payment_processing.integration;

import io.plagov.payment_processing.configuration.ContainersConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@Import(ContainersConfig.class)
@AutoConfigureMockMvc
class SearchPaymentsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    void setUp() {
        jdbcClient.sql("DELETE FROM payments").update();
    }

    @Test
    void shouldFindPaymentsByActiveStatus() throws Exception {
        var amount = new BigDecimal("100.00");
        var acceptedPaymentId = addPayment(amount, "ACCEPTED", null, null);
        var cancelledPaymentId = addPayment(amount, "CANCELLED", Timestamp.from(Instant.now()), new BigDecimal("1.05"));

        mockMvc.perform(get("/api/v1/payments")
                        .param("status", "ACCEPTED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id").value(acceptedPaymentId.toString()))
                .andExpect(jsonPath("$.[0].status").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.[0].cancelledAt").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.[0].cancellationFee").doesNotHaveJsonPath());
    }

    @Test
    void shouldFindPaymentsByCancelledStatus() throws Exception {
        var amount = new BigDecimal("100.00");
        var cancelledAt = Timestamp.from(Instant.now());
        var cancellationFee = new BigDecimal("1.05");

        var acceptedPaymentId = addPayment(amount, "ACCEPTED", null, null);
        var cancelledPaymentId = addPayment(amount, "CANCELLED", cancelledAt, cancellationFee);

        mockMvc.perform(get("/api/v1/payments")
                        .param("status", "CANCELLED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id").value(cancelledPaymentId.toString()))
                .andExpect(jsonPath("$.[0].status").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.[0].cancelledAt").isNotEmpty())
                .andExpect(jsonPath("$.[0].cancellationFee").isNotEmpty());
    }

    @Test
    void shouldFindPaymentsByAmountEqualTo() throws Exception {
        var amount = new BigDecimal("100.00");
        var cancelledAt = Timestamp.from(Instant.now());
        var cancellationFee = new BigDecimal("1.05");

        var acceptedPaymentIdOne = addPayment(amount, "ACCEPTED", null, null);
        var acceptedPaymentIdTwo = addPayment(amount, "ACCEPTED", null, null);
        var cancelledPaymentId = addPayment(amount, "CANCELLED", cancelledAt, cancellationFee);

        mockMvc.perform(get("/api/v1/payments")
                        .param("status", "ACCEPTED")
                        .param("isEqualTo", "100.00")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$.[0].id").value(acceptedPaymentIdOne.toString()))
                .andExpect(jsonPath("$.[1].id").value(acceptedPaymentIdTwo.toString()))
                .andExpect(jsonPath("$.[0].status").doesNotHaveJsonPath());
    }

    @Test
    void shouldFindPaymentsByAmountGreaterThan() throws Exception {
        var cancelledAt = Timestamp.from(Instant.now());
        var cancellationFee = new BigDecimal("1.05");

        var acceptedPaymentIdOne = addPayment(new BigDecimal("150.00"), "ACCEPTED", null, null);
        var acceptedPaymentIdTwo = addPayment(new BigDecimal("120.00"), "ACCEPTED", null, null);
        var cancelledPaymentId = addPayment(new BigDecimal("100.00"), "CANCELLED", cancelledAt, cancellationFee);

        mockMvc.perform(get("/api/v1/payments")
                        .param("status", "ACCEPTED")
                        .param("isGreaterThan", "125.00")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id").value(acceptedPaymentIdOne.toString()))
                .andExpect(jsonPath("$.[0].status").doesNotHaveJsonPath());
    }

    @Test
    void shouldFindPaymentsByAmountLessThan() throws Exception {
        var cancelledAt = Timestamp.from(Instant.now());
        var cancellationFee = new BigDecimal("1.05");

        var acceptedPaymentIdOne = addPayment(new BigDecimal("150.00"), "ACCEPTED", null, null);
        var acceptedPaymentIdTwo = addPayment(new BigDecimal("120.00"), "ACCEPTED", null, null);
        var cancelledPaymentId = addPayment(new BigDecimal("100.00"), "CANCELLED", cancelledAt, cancellationFee);

        mockMvc.perform(get("/api/v1/payments")
                        .param("status", "ACCEPTED")
                        .param("isLessThan", "125.00")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id").value(acceptedPaymentIdTwo.toString()))
                .andExpect(jsonPath("$.[0].status").doesNotHaveJsonPath());
    }

    private UUID addPayment(BigDecimal amount, String status, Timestamp cancelledAt, BigDecimal cancellationFee) {
        var query = """
                INSERT INTO payments (type, amount, currency, debtor_iban, creditor_iban, details, status,
                                      created_at, cancelled_at, cancellation_fee)
                VALUES (:type, :amount, :currency, :debtor_iban, :creditor_iban, :details, :status, :created_at,
                        :cancelled_at, :cancellation_fee)
                RETURNING id""";

        var paymentId = jdbcClient.sql(query)
                .param("type", "PESA")
                .param("amount", amount)
                .param("currency", "EUR")
                .param("debtor_iban", "EE382200221020145685")
                .param("creditor_iban", "LT121000011101001000")
                .param("details", "detail")
                .param("status", status)
                .param("created_at", Timestamp.from(Instant.now().minus(Duration.ofMinutes(2))))
                .param("cancelled_at", cancelledAt)
                .param("cancellation_fee", cancellationFee)
                .query(String.class)
                .single();

        return UUID.fromString(paymentId);
    }
}
