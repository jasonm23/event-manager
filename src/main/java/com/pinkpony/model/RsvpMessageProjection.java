package com.pinkpony.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "rsvpMessage", types = Rsvp.class)
public interface RsvpMessageProjection {

    String getUsername();
    String getResponse();

    @Value("#{target.username}, #{target.response}")
    String getMessage();
}
