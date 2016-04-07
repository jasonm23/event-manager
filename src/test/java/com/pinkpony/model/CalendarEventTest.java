package com.pinkpony.model;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.text.ParseException;
import java.util.Date;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CalendarEventTest {

    @Test
    public void testAddRsvp() throws Exception {
        CalendarEvent event = new CalendarEvent();
        Rsvp rsvp = new Rsvp();
        rsvp.setResponse("yes");
        rsvp.setUsername("Hermione");
        event.addRsvp(rsvp);

        assertThat(event.getRsvps(), hasItem(rsvp));
    }

    @Test
    public void cancel() {
        CalendarEvent event = new CalendarEvent();
        event.cancel();
        assertTrue(event.isCancelled());
    }

    @Test
    public void testConstructCorrectMessage() throws ParseException {
        String eventDateTimeString = "2025-05-06T06:33:11+08:00";
        Date date = CalendarEvent.dateFormat.parse(eventDateTimeString);
        CalendarEvent event = new CalendarEvent();
        event.setName("The Great Event");
        event.setDescription("Great Description");
        event.setVenue("Arrowhead Lounge");
        event.setCalendarEventDateTime(date);
        event.setCalendarEventDateTimeString(eventDateTimeString);
        event.setUsername("Holly");

        assertEquals("# The Great Event\\\\n\\\\n*Great Description*\\\\nAt 2025-05-06T06:33:11+08:00\\\\n\\\\n## Attendees:\\\\n{rsvps:\\\\n}", event.getMessage());
    }
}