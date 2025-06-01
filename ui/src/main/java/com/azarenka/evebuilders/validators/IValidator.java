package com.azarenka.evebuilders.validators;

import com.vaadin.flow.data.binder.Validator;

public interface IValidator<T> extends Validator<T> {

    boolean isValid(T value);
}
