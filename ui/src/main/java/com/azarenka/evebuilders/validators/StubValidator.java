package com.azarenka.evebuilders.validators;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;

public class StubValidator implements IValidator<Object>{
    @Override
    public boolean isValid(Object value) {
        return true;
    }

    @Override
    public ValidationResult apply(Object o, ValueContext valueContext) {
        return ValidationResult.ok();
    }
}
