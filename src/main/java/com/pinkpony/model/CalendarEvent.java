package com.pinkpony.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Entity
public class CalendarEvent implements Serializable {

    public final static String FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ssXXX";

    public final static DateFormat dateFormat = new SimpleDateFormat(CalendarEvent.FORMAT_STRING);
    static { dateFormat.setTimeZone(TimeZone.getTimeZone("UTC")); }

    @OneToMany(mappedBy = "calendarEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Rsvp> rsvps = new ArrayList<Rsvp>();

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "calendar_event_id_seq")
    @SequenceGenerator(name = "calendar_event_id_seq", sequenceName = "calendar_event_id_seq", allocationSize = 1)
    private Long id;

    private String name;
    private String description;

    @JsonIgnore
    private Date calendarEventDateTime;

    @JsonProperty("calendarEventDateTime")
    private String calendarEventDateTimeString;

    private String username;
    private String venue;

    private Boolean cancelled = false;

    public CalendarEvent(){}

    public CalendarEvent(String name){
        this.name = name;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @JsonIgnore
    public String getFormattedEventDateTime() {
        return dateFormat.format(getCalendarEventDateTime());
    }

    public String getCalendarEventDateTimeString() {
        return calendarEventDateTimeString;
    }

    public void setCalendarEventDateTimeString(String calendarEventDateTimeString) {
        this.calendarEventDateTimeString = calendarEventDateTimeString;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCalendarEventDateTime() {
        return calendarEventDateTime;
    }

    public void setCalendarEventDateTime(Date calendarEventDateTime) {
        this.calendarEventDateTime = calendarEventDateTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public void addRsvp(Rsvp rsvp) {
        rsvp.calendarEvent = this;
        rsvps.add(rsvp);
    }

    public List<Rsvp> getRsvps() {
        return rsvps;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public Boolean hasUsername(String username) {
        return this.getUsername().equals(username);
    }
}
