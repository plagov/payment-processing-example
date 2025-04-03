package io.plagov.payment_processing.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.validator.routines.IBANValidator;

public class ValidIbanValidator implements ConstraintValidator<ValidIban, String> {

    @Override
    public boolean isValid(String iban, ConstraintValidatorContext constraintValidatorContext) {
        IBANValidator ibanValidator = new IBANValidator();
        return iban != null && !iban.isEmpty() && ibanValidator.isValid(iban);
    }
}
