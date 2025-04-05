package io.plagov.payment_processing.dao;

import io.plagov.payment_processing.models.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface Dao<T, K> {

    K save(T t);

    Optional<T> getById(K k);

    T cancel(K k, Instant time, BigDecimal fee);

    List<T> queryPayments(PaymentStatus status,
                          Double isEqualTo,
                          Double isGreaterThan,
                          Double isLessThan);
}
