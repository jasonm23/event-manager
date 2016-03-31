package com.pinkpony.integration;


import com.jayway.restassured.RestAssured;
import com.pinkpony.PinkPonyApplication;
import com.pinkpony.model.CalendarEvent;
import com.pinkpony.model.Rsvp;
import com.pinkpony.repository.CalendarEventRepository;
import com.pinkpony.repository.RsvpRepository;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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

    public static DateFormat dateFormat = new SimpleDateFormat(CalendarEvent.FORMAT_STRING);
    static { dateFormat.setTimeZone(TimeZone.getTimeZone("UTC")); }
    public CalendarEvent existingCalendarEvent;
    public String calendarEventDateString = "2016-03-18T14:33:00+0000";
    public Date calendarEventDate;

    @Value("${local.server.port}")
    public int port;

    @Before
    public void setUp() throws ParseException {
        RestAssured.port = port;
        rsvpRepository.deleteAll();
        calendarEventRepository.deleteAll();
        calendarEventDate = (new DateTime(DateTimeZone.forID("UTC")).plusDays(1).toDate());
        calendarEventDateString = dateFormat.format(calendarEventDate);
        existingCalendarEvent = calendarEventRepository.save(makeCalendarEvent(calendarEventDate));
    }

    public CalendarEvent makeCalendarEvent(Date date) {
        CalendarEvent newCalendarEvent = new CalendarEvent();
        newCalendarEvent.setName("Spring Boot Night");
        newCalendarEvent.setDescription("Wanna learn how to boot?");
        newCalendarEvent.setVenue("Arrowhead Lounge");
        newCalendarEvent.setUsername("Holly");

        newCalendarEvent.setCalendarEventDateTime(date);
        newCalendarEvent.setCalendarEventDateTimeString(toUTCString(date));

        return newCalendarEvent;
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
