package com.pinkpony.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class RsvpTest {

    @Test
    public void setResponseLowercasesValue() throws Exception {
        Rsvp rsvp = new Rsvp();
        rsvp.setResponse("YES");
        assertEquals("yes", rsvp.getResponse());

        rsvp.setResponse("No");
        assertEquals("no", rsvp.getResponse());
    }
}