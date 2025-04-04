package io.plagov.payment_processing.dao;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface Dao<T, K> {

    K save(T t);

    Optional<T> getById(K k);

    T cancel(K k, Instant time, BigDecimal fee);
}
