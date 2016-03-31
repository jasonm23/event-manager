package com.pinkpony.validator;

import com.pinkpony.model.CalendarEvent;
import com.pinkpony.model.Rsvp;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;

import static org.hamcrest.Matchers.hasItemInArray;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class RsvpUpdateValidatorTest {
    RsvpUpdateValidator validator;

    @Before
    public void setUp() {
        validator = new RsvpUpdateValidator();
    }

    @Test
    public void validateCalendarEventDateTimeInPast() {
        CalendarEvent calendarEvent = new CalendarEvent();
        DateTime yesterday = (new DateTime()).minusDays(1);
        calendarEvent.setCalendarEventDateTime(yesterday.toDate());

        Rsvp rsvp = makeRsvp("username", "yes", calendarEvent);

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(rsvp, "Rsvp");
        validator.validate(rsvp, errors);

        assertTrue(errors.getErrorCount() > 0);
        assertThat(errors.getFieldError("calendarEvent").getCodes(), hasItemInArray("rsvp.calendarEvent.field.updateInThePast"));
    }

    private Rsvp makeRsvp(String username, String response, CalendarEvent calendarEvent) {
        Rsvp rsvp = new Rsvp();
        rsvp.setUsername(username);
        rsvp.setResponse(response);
        rsvp.calendarEvent = calendarEvent;
        return rsvp;
    }
}