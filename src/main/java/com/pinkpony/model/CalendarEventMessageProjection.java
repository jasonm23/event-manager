package com.pinkpony.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "messageCalendarEvent", types = CalendarEvent.class)
public interface CalendarEventMessageProjection {

    @Value("#{target.showMessage()}")
    String getMessage();

    @Value("channel")
    String getMessageType();
}
