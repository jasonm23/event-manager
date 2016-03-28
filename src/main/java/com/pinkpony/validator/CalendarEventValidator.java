package com.pinkpony.validator;

import com.pinkpony.model.CalendarEvent;
import org.springframework.expression.ParseException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CalendarEventValidator implements Validator {

    private final static DateFormat dateFormat = new SimpleDateFormat(CalendarEvent.FORMAT_STRING);

    @Override
    public boolean supports(Class<?> aClass) {
        return CalendarEvent.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "calendarEvent.name.field.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "description", "calendarEvent.description.field.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "calendarEventDateTimeString", "calendarEvent.calendarEventDateTime.field.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "venue", "calendarEvent.venue.field.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "calendarEvent.username.field.empty");

        CalendarEvent calendarEvent = (CalendarEvent)obj;
        if (!errors.hasFieldErrors("calendarEventDateTimeString")) {
            try {
                Date date = dateFormat.parse(calendarEvent.getCalendarEventDateTimeString());
                calendarEvent.setCalendarEventDateTime(date);
            } catch (Exception ex) {
                errors.rejectValue("calendarEventDateTimeString", "calendarEvent.calendarEventDateTime.field.invalid");
            }
        }
    }
}
