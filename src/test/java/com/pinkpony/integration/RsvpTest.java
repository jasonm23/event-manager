package com.pinkpony.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.pinkpony.PinkPonyApplication;
import com.pinkpony.model.CalendarEvent;
import com.pinkpony.model.Rsvp;
import com.pinkpony.repository.CalendarEventRepository;
import com.pinkpony.repository.RsvpRepository;
import org.json.JSONObject;
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
import java.util.HashMap;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PinkPonyApplication.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class RsvpTest {
    public String eventUri;
    @Autowired
    CalendarEventRepository calendarEventRepository;

    @Autowired
    RsvpRepository rsvpRepository;

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
    public void createRsvp() throws Exception {
        String eventUri = String.format("http://localhost:%s/events/%s", port, calendarEvent.getId());

        JSONObject params = new JSONObject();
        params.put("name", "Gabe");
        params.put("response", "yes");
        params.put("event", eventUri);

        given().
                contentType(ContentType.JSON).
                body(params.toString()).
                when().
                post("/rsvps").
                then().
                statusCode(201).
                body("_links.calendarEvent.href", containsString("/calendarEvent")).
                body("name", equalTo("Gabe")).
                body("response", equalTo("yes"));
    }

    @Test
    public void editRsvp() {
        Rsvp testRsvp = createTestRsvp("Bobby", "yes");

        given().
                contentType(ContentType.JSON).
                request().body("{\"name\":\"Bobby\", \"response\":\"no\"}").
                when().
                patch(String.format("/rsvps/%s", testRsvp.getId())).
                then().
                statusCode(200).
                body("response", equalTo("no")).
                body("name", equalTo("Bobby"));
    }

    private Rsvp createTestRsvp(String name, String response) {
        Rsvp rsvp = new Rsvp();
        rsvp.setName(name);
        rsvp.setResponse(response);
        rsvp.calendarEvent = calendarEvent;

        rsvpRepository.save(rsvp);
        return rsvp;
    }

    @Test
    public void rsvpWithNoFields() throws Exception {
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
