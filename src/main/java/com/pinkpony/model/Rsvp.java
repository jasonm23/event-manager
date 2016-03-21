package com.pinkpony.model;

import org.springframework.data.rest.core.annotation.RestResource;

import javax.annotation.Generated;
import javax.persistence.*;

@Entity
public class Rsvp {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    Long id;
    String name;
    String response;

    @ManyToOne
    @JoinColumn(name = "event_id")
    public Event event;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
