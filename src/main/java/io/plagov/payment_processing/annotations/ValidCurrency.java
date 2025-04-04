package io.plagov.payment_processing.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ValidCurrencyValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCurrency {
    String message() default "The provided currency is not supported";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
