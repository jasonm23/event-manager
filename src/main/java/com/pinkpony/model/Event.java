package com.pinkpony.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
public class Event implements Serializable{

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    Long id;

    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    String name;
    String description;
    Date eventDateTimeUTC;
    String organizer;
    String venue;

    public Event(){}

    public Event(String name){
        this.name = name;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getEventDateTimeUTC() {
        return df.format(eventDateTimeUTC);
    }

    public void setEventDateTimeUTC(String eventDateTimeUTC) throws ParseException {
        this.eventDateTimeUTC = df.parse(eventDateTimeUTC);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
