package com.pinkpony.integration;

import static org.hamcrest.Matchers.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.pinkpony.PinkPonyApplication;
import com.pinkpony.model.Event;
import com.pinkpony.model.Rsvp;
import com.pinkpony.repository.EventRepository;
import com.pinkpony.repository.RsvpRepository;
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
import java.util.HashMap;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PinkPonyApplication.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class EventCrudTest {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    RsvpRepository rsvpRepository;

    @Autowired
    MessageSource messageSource;

    private final static DateFormat dateFormat = new SimpleDateFormat(Event.FORMAT_STRING);
    Event existingEvent;
    String eventDateString = "2016-03-18T14:33:00+0000";
    Date eventDate;

    @Value("${local.server.port}")
    int port;

    static ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() throws ParseException {
        RestAssured.port = port;
        eventDate = dateFormat.parse(eventDateString);
        existingEvent = eventRepository.save(makeEvent(eventDate));
    }

    @After
    public void tearDown() {
        //TODO: why is this not doing whast we think?
        rsvpRepository.deleteAll();
        eventRepository.deleteAll();
    }

    private Event makeEvent(Date date) {
        Event newEvent = new Event();
        newEvent.setName("Spring Boot Night");
        newEvent.setDescription("Wanna learn how to boot?");
        newEvent.setVenue("Arrowhead Lounge");
        newEvent.setEventDateTime(date);
        newEvent.setOrganizer("Holly");

        return newEvent;
    }

    @Test
    public void createEvent() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("id", null);
        params.put("name", "Spring Boot Night");
        params.put("eventDateTime", eventDateString);
        params.put("description", "A Big Night of Eventness");
        params.put("organizer", "Joe");
        params.put("venue", "Arrowhead Lounge");

        given().
            contentType(ContentType.JSON).
            body(objectMapper.writeValueAsString(params)).
        when().
            post("/events").
        then().
            statusCode(201).
            body("name", equalTo("Spring Boot Night")).
            body("description", equalTo("A Big Night of Eventness")).
            body("venue", equalTo("Arrowhead Lounge")).
            body("eventDateTime", equalTo(eventDateString)).
            body("organizer", equalTo("Joe"));
    }

    @Test
    public void createRsvp() throws JsonProcessingException, ParseException {
        String eventUri = String.format("http://localhost:%s/events/%s", port, existingEvent.getId());

        HashMap<String, String> params = new HashMap<>();
        params.put("name", "Gabe");
        params.put("response", "yes");
        params.put("event", eventUri);

        given().
            contentType(ContentType.JSON).
            body(objectMapper.writeValueAsString(params)).
        when().
            post("/rsvps").
        then().
            statusCode(201).
            body("_links.event.href", containsString("/event")).
            body("name", equalTo("Gabe")).
            body("response", equalTo("yes"));
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
            get(String.format("/events/%s/rsvps", existingEvent.getId())).
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

    @Test
    public void editEvent() {
        given().
            contentType(ContentType.JSON).
            request().body("{\"name\":\"Mah Event Name is Changed\"}").
        when().
            patch(String.format("/events/%s", existingEvent.getId())).
        then().
            statusCode(200).
            body("name", equalTo("Mah Event Name is Changed"));
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
        rsvp.event = existingEvent;

        rsvpRepository.save(rsvp);
        return rsvp;
    }

    @Test
    public void badRequestOnMissingNameField() throws Exception {
        String jsonInput = "{\"id\":null,\"eventDateTime\":\"2016-03-18T14:33:00+0000\",\"description\":\"A Big Night of Eventness\",\"organizer\":\"Joe\",\"venue\":\"That amazing place\"}";

        given().
            contentType(ContentType.JSON).
            body(jsonInput).
        when().
            post("/events").
        then().
            statusCode(400).
            body("errors", hasSize(1)).
            body("errors[0].entity", equalTo("Event")).
            body("errors[0].message", equalTo(messageSource.getMessage("event.name.field.empty", null, LocaleContextHolder.getLocale()))).
            body("errors[0].property", equalTo("name")).
            body("errors[0].invalidValue", equalTo("null"));
    }

    @Test
    public void missingEventTimeOnlyReturnsOneError() throws Exception {
        String jsonInput = "{\"name\":\"Bob's big blowout\",\"description\":\"A Big Night of Eventness\",\"organizer\":\"Joe\",\"venue\":\"That amazing place\"}";

        given().
                contentType(ContentType.JSON).
                body(jsonInput).
        when().
                post("/events").
        then().
                statusCode(400).
                body("errors", hasSize(1)).
                body("errors[0].entity", equalTo("Event")).
                body("errors[0].message", equalTo(messageSource.getMessage("event.eventDateTime.field.empty", null, LocaleContextHolder.getLocale()))).
                body("errors[0].property", equalTo("eventDateTimeString")).
                body("errors[0].invalidValue", equalTo("null"));
    }

    @Test
    public void badRequestOnMissingDescriptionField() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("id", null);
        params.put("name", "name");
        params.put("eventDateTime", "2015-03-11T11:00:00+0000");
        params.put("organizer", "Joe");
        params.put("venue", "That amazing place");

        given().
                contentType(ContentType.JSON).
                body(objectMapper.writeValueAsString(params)).
                when().
                post("/events").
                then().
                statusCode(400).
                body("errors", hasSize(1)).
                body("errors[0].entity", equalTo("Event")).
                body("errors[0].message", equalTo(messageSource.getMessage("event.description.field.empty", null, LocaleContextHolder.getLocale()))).
                body("errors[0].property", equalTo("description")).
                body("errors[0].invalidValue", equalTo("null"));
    }

    @Test
    public void okRequestOnValidEventDateTimeFieldString() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("id", null);
        params.put("name", "name");
        params.put("description", "A Big Night of Eventness");
        params.put("eventDateTime", "2015-03-11T11:00:00+0000");
        params.put("organizer", "Joe");
        params.put("venue", "That amazing place");

        given().
                contentType(ContentType.JSON).
                body(objectMapper.writeValueAsString(params)).
                when().
                post("/events").
                then().
                statusCode(201);
    }

    @Test
    public void badRequestOnMissingEventDateTimeField() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("id", null);
        params.put("name", "name");
        params.put("description", "A Big Night of Eventness");
        params.put("organizer", "Joe");
        params.put("venue", "That amazing place");

        given().
                contentType(ContentType.JSON).
                body(objectMapper.writeValueAsString(params)).
                when().
                post("/events").
                then().
                statusCode(400).
                body("errors", hasSize(1)).
                body("errors[0].entity", equalTo("Event")).
                body("errors[0].message", equalTo(messageSource.getMessage("event.eventDateTime.field.empty", null, LocaleContextHolder.getLocale()))).
                body("errors[0].property", equalTo("eventDateTimeString")).
                body("errors[0].invalidValue", equalTo("null"));
    }

    @Test
    public void badRequestOnBlankEventDateTimeField() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("id", null);
        params.put("name", "name");
        params.put("description", "A Big Night of Eventness");
        params.put("organizer", "Joe");
        params.put("venue", "That amazing place");
        params.put("eventDateTime", "");

        given().
                contentType(ContentType.JSON).
                body(objectMapper.writeValueAsString(params)).
                when().
                post("/events").
                then().
                statusCode(400).
                body("errors", hasSize(1)).
                body("errors[0].entity", equalTo("Event")).
                body("errors[0].message", equalTo(messageSource.getMessage("event.eventDateTime.field.empty", null, LocaleContextHolder.getLocale()))).
                body("errors[0].property", equalTo("eventDateTimeString")).
                body("errors[0].invalidValue", equalTo(""));
    }

    @Test
    public void badRequestOnWrongFormattedEventDateTimeField() throws JsonProcessingException {
        Map<String, Object> params = new HashMap<>();
        params.put("id", null);
        params.put("name", "name");
        params.put("description", "A Big Night of Eventness");
        params.put("organizer", "Joe");
        params.put("venue", "That amazing place");
        params.put("eventDateTime", "2015-03-11T11:00:00");

        given().
                contentType(ContentType.JSON).
                body(objectMapper.writeValueAsString(params)).
                when().
                post("/events").
                then().
                statusCode(400).
                body("errors", hasSize(1)).
                body("errors[0].entity", equalTo("Event")).
                body("errors[0].message", equalTo(messageSource.getMessage("event.eventDateTime.field.invalid", null, LocaleContextHolder.getLocale()))).
                body("errors[0].property", equalTo("eventDateTimeString")).
                body("errors[0].invalidValue", equalTo("2015-03-11T11:00:00"));
    }

    @Test
    public void badRequestOnMissingVenueField() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("id", null);
        params.put("name", "Spring Boot Night");
        params.put("eventDateTime", eventDateString);
        params.put("description", "A Big Night of Eventness");
        params.put("organizer", "Joe");

        given().
                contentType(ContentType.JSON).
                body(objectMapper.writeValueAsString(params)).
                when().
                post("/events").
                then().
                statusCode(400).
                body("errors", hasSize(1)).
                body("errors[0].entity", equalTo("Event")).
                body("errors[0].message", equalTo(messageSource.getMessage("event.venue.field.empty", null, LocaleContextHolder.getLocale()))).
                body("errors[0].property", equalTo("venue")).
                body("errors[0].invalidValue", equalTo("null"));
    }

    @Test
    public void badRequestOnMissingOrganizerField() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("id", null);
        params.put("name", "Spring Boot Night");
        params.put("eventDateTime", eventDateString);
        params.put("description", "A Big Night of Eventness");
        params.put("venue", "Arrowhead Lounge");

        given().
                contentType(ContentType.JSON).
                body(objectMapper.writeValueAsString(params)).
                when().
                post("/events").
                then().
                statusCode(400).
                body("errors", hasSize(1)).
                body("errors[0].entity", equalTo("Event")).
                body("errors[0].message", equalTo(messageSource.getMessage("event.organizer.field.empty", null, LocaleContextHolder.getLocale()))).
                body("errors[0].property", equalTo("organizer")).
                body("errors[0].invalidValue", equalTo("null"));
    }

    @Test
    public void badRequestOnMultipleErrorsForField() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("id", null);
        params.put("description", "A Big Night of Eventness");
        params.put("eventDateTime", "");
        params.put("organizer", "Joe");
        params.put("venue", "Arrowhead Lounge");

        given().
                contentType(ContentType.JSON).
                body(objectMapper.writeValueAsString(params)).
                when().
                post("/events").
                then().
                statusCode(400).
                body("errors", hasSize(2)).
                body("errors[0].entity", equalTo("Event")).
                body("errors[0].message", equalTo(messageSource.getMessage("event.name.field.empty", null, LocaleContextHolder.getLocale()))).
                body("errors[0].property", equalTo("name")).
                body("errors[0].invalidValue", equalTo("null")).
                body("errors[1].entity", equalTo("Event")).
                body("errors[1].message", equalTo(messageSource.getMessage("event.eventDateTime.field.empty", null, LocaleContextHolder.getLocale()))).
                body("errors[1].property", equalTo("eventDateTimeString")).
                body("errors[1].invalidValue", equalTo(""));
    }
}
