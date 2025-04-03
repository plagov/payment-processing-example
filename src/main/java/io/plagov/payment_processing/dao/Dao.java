package io.plagov.payment_processing.dao;

public interface Dao<T, K> {

    K save(T t);
}
