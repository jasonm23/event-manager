package com.pinkpony.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;

@Projection(name = "inlineRsvps", types = CalendarEvent.class)
public interface CalendarEventProjection {
    String getName();
    String getDescription();
    String getUsername();
    String getVenue();

    @Value("#{ target.getFormattedEventDateTime() }")
    String getCalendarEventDateTime();
    Boolean getCancelled();

     List<Rsvp> getRsvps();
}
