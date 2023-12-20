package com.igloosec.smartguard.next.agentmanager.api.model;


import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import javax.validation.Validation;
import java.util.Collection;
import java.util.List;

@Component
public class CollectionValidator implements Validator {

    private final SpringValidatorAdapter validator;

    public CollectionValidator() {
        this.validator = new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator());
    }

    @Override
    public boolean supports(Class<?> claszz) {
        return Collection.class.isAssignableFrom(claszz);
    }

    @Override
    public void validate(Object o, Errors errors) {
        if (o instanceof List) {
            Collection collection = (Collection) o;
            for (Object obj : collection) {
                validator.validate(obj, errors);
            }
        }
    }
}
