package com.pinkpony.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.pinkpony.PinkPonyApplication;
import com.pinkpony.model.CalendarEvent;
import com.pinkpony.model.Rsvp;
import com.pinkpony.repository.CalendarEventRepository;
import com.pinkpony.repository.RsvpRepository;
import org.json.JSONObject;
import org.junit.After;
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
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PinkPonyApplication.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class CalendarEventCrudTest {

    @Autowired
    CalendarEventRepository calendarEventRepository;

    @Autowired
    RsvpRepository rsvpRepository;

    @Autowired
    MessageSource messageSource;

    private final static DateFormat dateFormat = new SimpleDateFormat(CalendarEvent.FORMAT_STRING);
    CalendarEvent existingCalendarEvent;
    String calendarEventDateString = "2016-03-18T14:33:00+0000";
    Date calendarEventDate;

    @Value("${local.server.port}")
    int port;

    static ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() throws ParseException {
        RestAssured.port = port;
        calendarEventDate = dateFormat.parse(calendarEventDateString);
        existingCalendarEvent = calendarEventRepository.save(makeCalendarEvent(calendarEventDate));
    }

    @After
    public void tearDown() {
        //TODO: why is this not doing whast we think?
        rsvpRepository.deleteAll();
        calendarEventRepository.deleteAll();
    }

    private CalendarEvent makeCalendarEvent(Date date) {
        CalendarEvent newCalendarEvent = new CalendarEvent();
        newCalendarEvent.setName("Spring Boot Night");
        newCalendarEvent.setDescription("Wanna learn how to boot?");
        newCalendarEvent.setVenue("Arrowhead Lounge");
        newCalendarEvent.setCalendarEventDateTime(date);
        newCalendarEvent.setOrganizer("Holly");

        return newCalendarEvent;
    }

    @Test
    public void createCalendarEvent() throws Exception {
        JSONObject params = new JSONObject();
        params.put("name", "Spring Boot Night");
        params.put("calendarEventDateTime", calendarEventDateString);
        params.put("description", "A Big Night of CalendarEventness");
        params.put("organizer", "Joe");
        params.put("venue", "Arrowhead Lounge");

        given().
            contentType(ContentType.JSON).
            body(params.toString()).
        when().log().all().
            post("/calendarEvents").
        then().
            statusCode(201).
            body("name", equalTo("Spring Boot Night")).
            body("description", equalTo("A Big Night of CalendarEventness")).
            body("venue", equalTo("Arrowhead Lounge")).
            body("calendarEventDateTime", equalTo(calendarEventDateString)).
            body("organizer", equalTo("Joe"));
    }

    @Test
    public void eventsListWithRSVPs() {
        // When an event has RSVPs...
        createTestRsvp("Billy", "yes");
        createTestRsvp("Sarah", "yes");
        createTestRsvp("Jo", "no");
        createTestRsvp("Colin", "yes");
        createTestRsvp("Trudy", "no");
        createTestRsvp("Heng", "no");

        given().
            contentType(ContentType.JSON).
        when().
            get(String.format("/calendarEvents/%s/rsvps", existingCalendarEvent.getId())).
        then().
            statusCode(200).
            body("_embedded.rsvps[0].name", containsString("Billy")).
            body("_embedded.rsvps[0].response", containsString("yes")).
            body("_embedded.rsvps[1].name", containsString("Sarah")).
            body("_embedded.rsvps[1].response", containsString("yes")).
            body("_embedded.rsvps[2].name", containsString("Jo")).
            body("_embedded.rsvps[2].response", containsString("no")).
            body("_embedded.rsvps[3].name", containsString("Colin")).
            body("_embedded.rsvps[3].response", containsString("yes")).
            body("_embedded.rsvps[4].name", containsString("Trudy")).
            body("_embedded.rsvps[4].response", containsString("no")).
            body("_embedded.rsvps[5].name", containsString("Heng")).
            body("_embedded.rsvps[5].response", containsString("no"));
    }

//   SF>: This test belongs to a future story. See #116011543
//        This breaks current patch behaviour
//    @Test
//    public void editCalendarEvent() {
//        given().
//            contentType(ContentType.JSON).
//            request().body("{\"name\":\"Mah CalendarEvent Name is Changed\"}").
//        when().
//            patch(String.format("/calendarEvents/%s", existingCalendarEvent.getId())).
//        then().
//            statusCode(200).
//            body("name", equalTo("Mah CalendarEvent Name is Changed"));
//    }

    private Rsvp createTestRsvp(String name, String response) {
        Rsvp rsvp = new Rsvp();
        rsvp.setName(name);
        rsvp.setResponse(response);
        rsvp.calendarEvent = existingCalendarEvent;

        rsvpRepository.save(rsvp);
        return rsvp;
    }

    @Test
    public void badRequestOnErrors() throws Exception {
        JSONObject params = new JSONObject();
        params.put("description", "A Big Night of Eventness");
        params.put("eventDateTime", "");
        params.put("organizer", "Joe");
        params.put("venue", "Arrowhead Lounge");

        given().
                contentType(ContentType.JSON).
                body(params.toString()).
                when().
                post("/calendarEvents").
        then().
                statusCode(400).
                body("errors", hasSize(2)).
                body("errors[0].entity", equalTo("CalendarEvent")).
                body("errors[0].message", equalTo(messageSource.getMessage("calendarEvent.name.field.empty", null, LocaleContextHolder.getLocale()))).
                body("errors[0].property", equalTo("name")).
                body("errors[0].invalidValue", equalTo("null")).
                body("errors[1].entity", equalTo("CalendarEvent")).
                body("errors[1].message", equalTo(messageSource.getMessage("calendarEvent.calendarEventDateTime.field.empty", null, LocaleContextHolder.getLocale()))).
                body("errors[1].property", equalTo("calendarEventDateTimeString")).
                body("errors[1].invalidValue", equalTo(""));
    }
}