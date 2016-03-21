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

import java.text.ParseException;
import java.util.HashMap;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

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
    public void rsvpYes() throws JsonProcessingException, ParseException {
        ObjectMapper  mapper = new ObjectMapper();

        HashMap<String, String> body = new HashMap<>();
        body.put("name", "Gabe");
        body.put("response", "yes");
        body.put("event", String.format("http://localhost:%s/events/%s", port, existingEvent.getId()));

        given().log().all().
            contentType(ContentType.JSON).
            body(mapper.writeValueAsString(body)).
        when().
            post(String.format("/rsvps")).
        then().
            statusCode(201).
            body("name", equalTo("Gabe")).
            body("response", equalTo("yes"));
    }

    @Test
    public void eventsListWithRSVPs() {

    }

    private void createTestRsvp(String name, String response) {
        Rsvp rsvp = new Rsvp();
        rsvp.setName(name);
        rsvp.setResponse(response);

        rsvpRepository.save(rsvp);
    }

}
