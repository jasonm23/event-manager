package com.pinkpony.validator;

import com.pinkpony.model.CalendarEvent;
import com.pinkpony.model.Rsvp;
import org.joda.time.DateTime;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.Date;

public class RsvpUpdateValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return Rsvp.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object object, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "rsvp.username.field.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "response", "rsvp.response.field.empty");

        Rsvp rsvp = (Rsvp) object;
        if (! (rsvp.getResponse().equals("yes") || rsvp.getResponse().equals("no"))) {
            errors.rejectValue("response", "rsvp.response.field.invalidValue");
        }

        CalendarEvent calendarEvent = rsvp.calendarEvent;
        Date timeNow = new DateTime().toDate();
        if ( calendarEvent != null && timeNow.compareTo(calendarEvent.getCalendarEventDateTime()) > 0 ) {
            errors.rejectValue("calendarEvent", "rsvp.calendarEvent.field.updateInThePast");
        }
    }
}
