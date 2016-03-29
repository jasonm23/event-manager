package com.pinkpony.repository;

import com.pinkpony.model.CalendarEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "eventMessage", types = CalendarEvent.class)
public interface CalendarEventMessageProjection {
    String getName();
    String getDescription();
    String getOrganizer();
    String getVenue();

    @Value("event #{target.name} created")
    String getMessage();
}
