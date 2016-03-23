package com.pinkpony.validator;

import com.pinkpony.model.Rsvp;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class RsvpValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
       return Rsvp.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object object, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "rsvp.name.field.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "response", "rsvp.response.field.empty");
        Rsvp rsvp = (Rsvp) object;
        if (! (rsvp.getResponse().equals("yes") || rsvp.getResponse().equals("no"))) {
            errors.rejectValue("response", "rsvp.response.field.invalidValue");
        }
    }
}
