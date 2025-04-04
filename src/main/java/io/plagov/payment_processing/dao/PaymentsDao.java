package io.plagov.payment_processing.dao;

import io.plagov.payment_processing.entities.PaymentEntity;
import io.plagov.payment_processing.models.enums.PaymentStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
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

    @Override
    public Optional<PaymentEntity> getById(UUID paymentId) {
        var query = """
                SELECT
                    id, type, amount, currency, debtor_iban, creditor_iban, details,
                    status, created_at, cancelled_at, cancellation_fee
                FROM payments WHERE id = :paymentId""";
        return jdbcClient.sql(query)
                .param("paymentId", paymentId)
                .query(PaymentEntity.class)
                .optional();
    }

    @Override
    public PaymentEntity cancel(UUID uuid, Instant cancellationTime, BigDecimal fee) {
        var query = """
                UPDATE payments
                SET status = :status, cancelled_at = :cancelled_at, cancellation_fee = :cancellation_fee
                WHERE id = :id
                RETURNING id, type, amount, currency, debtor_iban, creditor_iban, details,
                          status, created_at, cancelled_at, cancellation_fee""";

        return jdbcClient.sql(query)
                .param("status", PaymentStatus.CANCELLED.name())
                .param("cancelled_at", Timestamp.from(cancellationTime))
                .param("cancellation_fee", fee)
                .param("id", uuid)
                .query(PaymentEntity.class)
                .single();
    }
}
