package io.plagov.payment_processing.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.util.List;

public class ValidCurrencyValidator implements ConstraintValidator<ValidCurrency, Money> {

    @Override
    public boolean isValid(Money money, ConstraintValidatorContext constraintValidatorContext) {
        return money != null && List.of(CurrencyUnit.EUR, CurrencyUnit.USD).contains(money.getCurrencyUnit());
    }
}
