package com.azarenka.evebuilders.validators;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;

import java.util.Objects;

public class RequiredValidator implements IValidator<Object> {

    private final String errorMessage;

    public RequiredValidator(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public ValidationResult apply(Object value, ValueContext valueContext) {
        if (isValid(value)) {
            return ValidationResult.ok();
        }
        return ValidationResult.error(errorMessage);
    }

    @Override
    public boolean isValid(Object value) {
        boolean result = Objects.nonNull(value);
        if (result && value instanceof String) {
            result = !((String) value).isEmpty();
        }
        return result;
    }

}
