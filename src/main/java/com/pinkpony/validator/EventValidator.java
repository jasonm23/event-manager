package com.pinkpony.validator;

import com.pinkpony.model.Event;
import org.springframework.expression.ParseException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.Date;

public class EventValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return Event.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object obj, Errors errors) {

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "event.name.field.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "description", "event.description.field.empty");

        Event event = (Event) obj;


//      String organizer;

//      String venue;

    }
}
