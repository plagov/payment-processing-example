package io.plagov.payment_processing.dao;

import io.plagov.payment_processing.entities.PaymentEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

@Component
public class PaymentsDao implements Dao<PaymentEntity, UUID> {

    private final JdbcClient jdbcClient;

    public PaymentsDao(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public UUID save(PaymentEntity createPayment) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String query = """
                INSERT INTO payments (type, amount, currency, debtor_iban, creditor_iban, details, status, created_at)
                VALUES (:type, :amount, :currency, :debtor_iban, :creditor_iban, :details, :status, :created_at)""";
        jdbcClient.sql(query)
                .param("type", createPayment.getType().name())
                .param("amount", createPayment.getAmount())
                .param("currency", createPayment.getCurrency())
                .param("debtor_iban", createPayment.getDebtorIban())
                .param("creditor_iban", createPayment.getCreditorIban())
                .param("details", createPayment.getDetails())
                .param("status", createPayment.getStatus().name())
                .param("created_at", Timestamp.from(createPayment.getCreatedAt()))
                .update(keyHolder);

        Map<String, Object> keys = keyHolder.getKeys();
        if (keys == null || keys.get("id") == null) {
            throw new RuntimeException("Failed to retrieve generated UUID for payment");
        }
        return UUID.fromString(keyHolder.getKeys().get("id").toString());
    }
}
