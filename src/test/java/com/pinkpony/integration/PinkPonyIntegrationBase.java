package com.pinkpony.integration;


import com.jayway.restassured.RestAssured;
import com.pinkpony.PinkPonyApplication;
import com.pinkpony.model.CalendarEvent;
import com.pinkpony.model.Rsvp;
import com.pinkpony.repository.CalendarEventRepository;
import com.pinkpony.repository.RsvpRepository;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.time.format.DateTimeFormatter.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PinkPonyApplication.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
abstract class PinkPonyIntegrationBase {

    @Autowired
    public CalendarEventRepository calendarEventRepository;

    @Autowired
    public RsvpRepository rsvpRepository;

    @Autowired
    public MessageSource messageSource;

    public static DateFormat dateFormat = CalendarEvent.dateFormat;
    public CalendarEvent existingCalendarEvent;
    public String calendarEventDateString;
    public Date calendarEventDate;

    @Value("${local.server.port}")
    public int port;

    @Before
    public void setUp() throws ParseException {
        RestAssured.port = port;
        rsvpRepository.deleteAll();
        calendarEventRepository.deleteAll();

        RestAssured.port = port;
//        DateTimeFormatter.ofPattern(CalendarEvent.FORMAT_STRING);
        calendarEventDate = (new DateTime(DateTimeZone.forID("UTC")).plusDays(1).toDate());
        calendarEventDateString = dateFormat.format(calendarEventDate);
        existingCalendarEvent = calendarEventRepository.save(makeCalendarEvent(calendarEventDate));
    }

    public CalendarEvent makeCalendarEvent(Date date) {
        CalendarEvent newCalendarEvent = new CalendarEvent();
        newCalendarEvent.setName("Spring Boot Night");
        newCalendarEvent.setDescription("Wanna learn how to boot?");
        newCalendarEvent.setVenue("Arrowhead Lounge");
        newCalendarEvent.setCalendarEventDateTime(date);
        newCalendarEvent.setCalendarEventDateTimeString(dateFormat.format(date));
        newCalendarEvent.setUsername("Holly");
        return newCalendarEvent;
    }

    public CalendarEvent makePastCalendarEvent() {
        return makeCalendarEvent(new DateTime(DateTimeZone.forID("UTC")).minusDays(2).toDate());
    }

    public CalendarEvent addRsvp(CalendarEvent event, String response, String username) {
        Rsvp rsvp = new Rsvp();
        rsvp.setUsername(username);
        rsvp.setResponse(response);
        event.addRsvp(rsvp);
        return calendarEventRepository.save(event);
    }

    public Rsvp createTestRsvp(String response, String username) {
        Rsvp rsvp = new Rsvp();
        rsvp.setUsername(username);
        rsvp.setResponse(response);
        rsvp.calendarEvent = existingCalendarEvent;
        return rsvpRepository.save(rsvp);
    }

    private String toUTCString(Date date) {
        DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return outputFormat.format(date);
    }

}
