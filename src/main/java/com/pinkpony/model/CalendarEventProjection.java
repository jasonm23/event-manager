package com.pinkpony.model;

import com.pinkpony.model.CalendarEvent;
import com.pinkpony.model.Rsvp;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;

@Projection(name = "inlineRsvp", types = CalendarEvent.class)
public interface CalendarEventProjection {
    String getName();
    String getDescription();
    String getUsername();
    String getVenue();
    Boolean getCancelled();

    List<Rsvp> getRsvps();
}
