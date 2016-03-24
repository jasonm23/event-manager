package com.pinkpony.validator;

import com.pinkpony.model.Event;
import org.springframework.expression.ParseException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EventValidator implements Validator {

    private final static DateFormat dateFormat = new SimpleDateFormat(Event.FORMAT_STRING);

    @Override
    public boolean supports(Class<?> aClass) {
        return Event.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "event.name.field.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "description", "event.description.field.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "eventDateTimeString", "event.eventDateTime.field.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "venue", "event.venue.field.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "organizer", "event.organizer.field.empty");

        Event event = (Event)obj;
        if (!errors.hasFieldErrors("eventDateTimeString")) {
            try {
                Date date = dateFormat.parse(event.getEventDateTimeString());
                event.setEventDateTime(date);
            } catch (Exception ex) {
                errors.rejectValue("eventDateTimeString", "event.eventDateTime.field.invalid");
            }
        }
    }
}
