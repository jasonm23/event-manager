package com.pinkpony.integration;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.HashMap;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PinkPonyApplication.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
@ActiveProfiles(profiles = "test")
public class EventCrudTest {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    RsvpRepository rsvpRepository;

    Event existingEvent;
    String eventDate = "2016-04-18T14:33:00";

    @Value("${local.server.port}")
    int port;

    @Before
    public void setUp() throws ParseException {
        RestAssured.port = port;

        existingEvent = new Event();
        existingEvent.setName("BG Night");
        existingEvent.setDescription("A Big Night of Eventness");
        existingEvent.setVenue("That amazing place");
        existingEvent.setEventDateTimeUTC(eventDate);
        existingEvent.setOrganizer("Joe");
        eventRepository.save(existingEvent);
    }

    @After
    public void tearDown() {
        //TODO: why is this not doing whast we think?
        rsvpRepository.deleteAll();
        eventRepository.deleteAll();
    }

    @Test
    public void createEvent() throws JsonProcessingException, ParseException {
        ObjectMapper  mapper = new ObjectMapper();

        Event newEvent = new Event();
        newEvent.setName("Spring Boot Night");
        newEvent.setDescription("Wanna learn how to boot?");
        newEvent.setVenue("Arrowhead Lounge");
        newEvent.setEventDateTimeUTC(eventDate);
        newEvent.setOrganizer("Holly");

        given().
            contentType(ContentType.JSON).
            body(mapper.writeValueAsString(newEvent)).
        when().
            post("/events").
        then().
            statusCode(201).
            body("name", equalTo("Spring Boot Night")).
            body("description", equalTo("Wanna learn how to boot?")).
            body("venue", equalTo("Arrowhead Lounge")).
            body("eventDateTimeUTC", equalTo(eventDate)).
            body("organizer", equalTo("Holly"));
    }

    @Test
    public void createRsvp() throws JsonProcessingException, ParseException {
        ObjectMapper  mapper = new ObjectMapper();
        String eventUri = String.format("http://localhost:%s/events/%s", port, existingEvent.getId());

        HashMap<String, String> body = new HashMap<>();
        body.put("name", "Gabe");
        body.put("response", "yes");
        body.put("event", eventUri);

        given().
            contentType(ContentType.JSON).
            body(mapper.writeValueAsString(body)).
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
        createTestRsvp("Billy", "Yes");
        createTestRsvp("Sarah", "Yes");
        createTestRsvp("Jo", "No");
        createTestRsvp("Colin", "Yes");
        createTestRsvp("Trudy", "No");
        createTestRsvp("Heng", "No");

        given().
            contentType(ContentType.JSON).
        when().
            get(String.format("/events/%s/rsvps", existingEvent.getId())).
        then().
            statusCode(200).
            body("_embedded.rsvps[0].name", containsString("Billy")).
            body("_embedded.rsvps[0].response", containsString("Yes")).
            body("_embedded.rsvps[1].name", containsString("Sarah")).
            body("_embedded.rsvps[1].response", containsString("Yes")).
            body("_embedded.rsvps[2].name", containsString("Jo")).
            body("_embedded.rsvps[2].response", containsString("No")).
            body("_embedded.rsvps[3].name", containsString("Colin")).
            body("_embedded.rsvps[3].response", containsString("Yes")).
            body("_embedded.rsvps[4].name", containsString("Trudy")).
            body("_embedded.rsvps[4].response", containsString("No")).
            body("_embedded.rsvps[5].name", containsString("Heng")).
            body("_embedded.rsvps[5].response", containsString("No"));
    }

    @Test
    public void editEvent() {
        given().
            contentType(ContentType.JSON).
            request().body("{\"name\":\"Mah Event Name is Changed\"}").
        when().
            patch(String.format("/events/%s", existingEvent.getId())).
        then().log().all().
            statusCode(200).
            body("name", equalTo("Mah Event Name is Changed"));
    }

    @Test
    public void editRsvp() {
        Rsvp testRsvp = createTestRsvp("Bobby", "yes");

        given().
            contentType(ContentType.JSON).
            request().body("{\"name\":\"Bobby\",\"response\":\"no\"}").
        when().
            patch(String.format("/rsvps/%s", testRsvp.getId())).
        then().log().all().
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

}
