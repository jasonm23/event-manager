package com.pinkpony.model;

import javax.persistence.*;

@Entity
public class Rsvp {
    @ManyToOne
    @JoinColumn(name = "calendar_event_id")
    public CalendarEvent calendarEvent;
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
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
}
