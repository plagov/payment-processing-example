package io.plagov.payment_processing.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.joda.money.Money;

public class PositiveAmountValidator implements ConstraintValidator<PositiveAmount, Money> {

    @Override
    public boolean isValid(Money money, ConstraintValidatorContext constraintValidatorContext) {
        return money != null && money.isPositive();
    }
}
