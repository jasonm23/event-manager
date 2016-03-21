package com.pinkpony.model;

import javax.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Event implements Serializable {

    private static final String FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";
    public final static DateFormat dateFormat = new SimpleDateFormat(FORMAT_STRING);

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Rsvp> rsvps = new ArrayList<Rsvp>();

    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private String name;
    private String description;

    @DateTimeFormat(pattern=FORMAT_STRING)
    private Date eventDateTimeUTC;

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
        return dateFormat.format(eventDateTimeUTC);
    }

    public void setEventDateTimeUTC(String eventDateTimeUTC) throws ParseException {
        this.eventDateTimeUTC = dateFormat.parse(eventDateTimeUTC);
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
