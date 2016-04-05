package com.pinkpony.validator;

import com.pinkpony.model.CalendarEvent;
import com.pinkpony.model.Rsvp;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.format.datetime.standard.DateTimeContext;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;

import static org.hamcrest.Matchers.hasItemInArray;
import static org.junit.Assert.*;

public class RsvpCreateValidatorTest {
    RsvpCreateValidator validator;

    @Before
    public void setUp() {
        validator = new RsvpCreateValidator();
    }

    @Test
    public void validateBlankCalendarEvent() {

        Rsvp rsvp = makeRsvp("username", "yes", null);

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(rsvp, "Rsvp");
        validator.validate(rsvp, errors);

        assertTrue(errors.getErrorCount() > 0);
        assertThat(errors.getFieldError("calendarEvent").getCodes(), hasItemInArray("rsvp.calendarEvent.field.invalidValue"));
    }

    @Test
    public void validateCalendarEventDateTimeInPast() {
        CalendarEvent calendarEvent = new CalendarEvent();
        DateTime yesterday = (new DateTime()).minusDays(1);
        calendarEvent.setCalendarEventDateTime(yesterday.toDate());

        Rsvp rsvp = makeRsvp("username", "yes", calendarEvent);

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(rsvp, "Rsvp");
        validator.validate((Object)rsvp, errors);

        assertTrue(errors.getErrorCount() > 0);
        assertThat(errors.getFieldError("calendarEvent").getCodes(), hasItemInArray("rsvp.calendarEvent.field.createInThePast"));
    }

    private Rsvp makeRsvp(String username, String response, CalendarEvent calendarEvent) {
        Rsvp rsvp = new Rsvp();
        rsvp.setUsername(username);
        rsvp.setResponse(response);
        rsvp.calendarEvent = calendarEvent;
        return rsvp;
    }
}
