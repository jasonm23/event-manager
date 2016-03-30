package com.pinkpony.model;

import com.pinkpony.model.Rsvp;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "inlineRsvp", types = Rsvp.class)
public interface RsvpProjection {

    String getName();
    String getResponse();
}
