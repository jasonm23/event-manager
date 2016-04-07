package com.pinkpony.validator;

import com.pinkpony.model.Rsvp;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.Arrays;

public class RsvpBaseValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return Rsvp.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object object, Errors errors) {
        commonValidate(object, errors);
    }

    protected void commonValidate(Object object, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "rsvp.username.field.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "response", "rsvp.response.field.empty");

        Rsvp rsvp = (Rsvp) object;
        if (rsvp.getResponse() != null && ! responseIsValid(rsvp)) {
            errors.rejectValue("response", "rsvp.response.field.invalidValue");
        }
    }

    private boolean responseIsValid(Rsvp rsvp) {
        String response = rsvp.getResponse();
        return response.equals("yes") || response.equals("no");
    }
}
