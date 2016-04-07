package com.pinkpony.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "inlineRsvp", types = Rsvp.class)
public interface RsvpProjection {

    String getUsername();
    String getResponse();

    @Value("#{target.username}, #{target.response}")
    String getMessage();
}
