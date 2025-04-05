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
    void shouldFindPaymentsByStatus() throws Exception {
        var acceptedPaymentId = addPayment("ACCEPTED");
        var cancelledPaymentId = addPayment("CANCELLED");

        mockMvc.perform(get("/api/v1/payments")
                        .param("status", "ACCEPTED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0].id").value(acceptedPaymentId.toString()))
                .andExpect(jsonPath("$.[0].status").value("ACCEPTED"));
    }

    private UUID addPayment(String status) {
        var query = """
                INSERT INTO payments (type, amount, currency, debtor_iban, creditor_iban, details, status, created_at)
                VALUES (:type, :amount, :currency, :debtor_iban, :creditor_iban, :details, :status, :created_at)
                RETURNING id""";

        var paymentId = jdbcClient.sql(query)
                .param("type", "PESA")
                .param("amount", new BigDecimal("100.00"))
                .param("currency", "EUR")
                .param("debtor_iban", "EE382200221020145685")
                .param("creditor_iban", "LT121000011101001000")
                .param("details", "detail")
                .param("status", status)
                .param("created_at", Timestamp.from(Instant.now()))
                .query(String.class)
                .single();

        return UUID.fromString(paymentId);
    }
}
