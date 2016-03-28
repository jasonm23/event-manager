package com.pinkpony.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.pinkpony.PinkPonyApplication;
import com.pinkpony.model.CalendarEvent;
import com.pinkpony.repository.CalendarEventRepository;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PinkPonyApplication.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class RsvpTest {
    public String eventUri;
    @Autowired
    CalendarEventRepository calendarEventRepository;

    @Autowired
    MessageSource messageSource;

    CalendarEvent calendarEvent;
    private final static DateFormat dateFormat = new SimpleDateFormat(CalendarEvent.FORMAT_STRING);
    String eventDateString = "2016-04-18T14:33:00+0000";
    Date calendarEventDate;
    @Value("${local.server.port}")
    int port;

    @Before
    public void setUp() throws ParseException {
        RestAssured.port = port;
        calendarEventDate = dateFormat.parse(eventDateString);

        calendarEvent = new CalendarEvent();
        calendarEvent.setName("BG Night");
        calendarEvent.setDescription("A Big Night of CalendarEventness");
        calendarEvent.setVenue("That amazing place");
        calendarEvent.setCalendarEventDateTime(calendarEventDate);
        calendarEvent.setOrganizer("Joe");
        calendarEventRepository.save(calendarEvent);

        eventUri = String.format("http://localhost:%s/calendarEvents/%s", port, calendarEvent.getId());
    }

    @Test
    public void rsvpWithNoFields() throws JsonProcessingException, ParseException {
        JSONObject json = new JSONObject();
        json.put("name", "");
        json.put("response", "");
        //TODO: should we just test invalid post here? not all missing fields since this is duplicating the rsvpval unit tsts?

        given().
                contentType(ContentType.JSON).
                body(json.toString()).
            when().
                post(String.format("/rsvps")).
            then().
                statusCode(400).
                body("errors", hasSize(3)).
                body("errors[0].entity", equalTo("Rsvp")).
                body("errors[0].message", equalTo(messageSource.getMessage("rsvp.name.field.empty", null, LocaleContextHolder.getLocale()))).
                body("errors[0].property", equalTo("name")).
                body("errors[0].invalidValue", equalTo("")).
                body("errors[1].message", equalTo(messageSource.getMessage("rsvp.response.field.empty", null, LocaleContextHolder.getLocale()))).
                body("errors[1].property", equalTo("response")).
                body("errors[1].invalidValue", equalTo(""));

        //TODO: add more assertions about the shape and content of error messages in response
    }
}
