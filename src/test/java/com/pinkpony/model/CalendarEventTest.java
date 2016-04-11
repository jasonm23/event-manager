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
    public void testMessage() {
        CalendarEvent event = new CalendarEvent();
        event.setName("Game Night");
        event.setVenue("The deck");
        event.setCalendarEventDateTimeString("2016-11-12T11:22:33+08:00");
        event.setId(1L);

        assertEquals("Game Night at The deck on 2016-11-12T11:22:33+08:00 view details via /marvin event details 1", event.showMessage());
    }
}