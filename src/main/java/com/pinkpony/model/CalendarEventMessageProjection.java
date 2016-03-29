package com.pinkpony.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pinkpony.model.CalendarEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "eventMessage", types = { CalendarEvent.class })
public interface CalendarEventMessageProjection {
    String getName();
    String getDescription();
    String getUsername();
    String getVenue();

    @JsonProperty("calendarEventDateTime")
    String getCalendarEventDateTimeString();

    @Value("event #{target.name} created")
    String getMessage();

    // NOTE: What is message type / how is it defined?
    @Value("channel")
    @JsonProperty("message_type")
    String getMessageType();
}
