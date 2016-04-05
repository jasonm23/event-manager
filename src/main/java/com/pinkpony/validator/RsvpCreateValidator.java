package com.pinkpony.validator;

import com.pinkpony.model.CalendarEvent;
import com.pinkpony.model.Rsvp;
import org.joda.time.DateTime;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import java.util.Date;

public class RsvpCreateValidator extends RsvpBaseValidator {

    @Override
    public void validate(Object object, Errors errors) {
        commonValidate(object, errors);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "calendarEvent", "rsvp.calendarEvent.field.invalidValue");

        Rsvp rsvp = (Rsvp) object;
        CalendarEvent calendarEvent = rsvp.calendarEvent;

        Date timeNow = new DateTime().toDate();
        if ( calendarEvent != null && timeNow.compareTo(calendarEvent.getCalendarEventDateTime()) > 0 ) {
            errors.rejectValue("calendarEvent", "rsvp.calendarEvent.field.createInThePast");
        }
    }
}
