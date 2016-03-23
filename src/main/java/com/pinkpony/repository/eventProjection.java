package com.pinkpony.repository;

import com.pinkpony.model.Event;
import com.pinkpony.model.Rsvp;
import org.springframework.data.rest.core.config.Projection;

import java.util.Date;
import java.util.List;

/**
 * Created by neo on 23/3/16.
 */
@Projection(name = "inlineRsvp", types = Event.class)
public interface EventProjection {
    String getName();
    String getDescription();
    String getOrganizer();
    String getVenue();

    List<Rsvp> getRsvps();
}
