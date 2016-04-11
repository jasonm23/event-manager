package com.pinkpony.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;

@Entity
public class Rsvp {
    @ManyToOne
    @JoinColumn(name = "calendar_event_id")
    public CalendarEvent calendarEvent;
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "rsvp_id_seq")
    @SequenceGenerator(name = "rsvp_id_seq", sequenceName = "rsvp_id_seq", allocationSize = 1)
    Long id;
    String username;
    String response;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response.toLowerCase();
    }

    public Long getId() {
        return id;
    }

    public CalendarEvent getCalendarEvent() {
        return this.calendarEvent;
    }
}
