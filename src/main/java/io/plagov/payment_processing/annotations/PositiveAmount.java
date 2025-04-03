package io.plagov.payment_processing.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PositiveAmountValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PositiveAmount {
    String message() default "The payment amount must be positive";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
