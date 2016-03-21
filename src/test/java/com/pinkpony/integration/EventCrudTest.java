package com.pinkpony.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.pinkpony.PinkPonyApplication;
import com.pinkpony.model.Event;
import com.pinkpony.repository.EventRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
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

public class EventCrudTest {

    @Autowired
    EventRepository eventRepository;

    Event event;
    String eventDate = "2016-04-18T14:33:00";

    @Value("${local.server.port}")
    int port;

    @Before
    public void setUp() throws ParseException {
        RestAssured.port = port;

        event = new Event();
        event.setName("BG Night");
        event.setDescription("A Big Night of Eventness");
        event.setVenue("That amazing place");
        event.setEventDateTimeUTC(eventDate);
        event.setOrganizer("Joe");
        eventRepository.save(event);
    }

    @Test
    public void createEvent() throws JsonProcessingException, ParseException {
        ObjectMapper  mapper = new ObjectMapper();

        given().
            contentType(ContentType.JSON).
            body(mapper.writeValueAsString(event)).
        when().
            post("/events").
        then().
            statusCode(201).
            body("name", equalTo("BG Night")).
            body("description", equalTo("A Big Night of Eventness")).
            body("venue", equalTo("That amazing place")).
            body("eventDateTimeUTC", equalTo(eventDate)).
            body("organizer", equalTo("Joe"));
    }

    @Test
    public void rsvpYes() throws JsonProcessingException, ParseException {
        ObjectMapper  mapper = new ObjectMapper();

        HashMap<String, String> body = new HashMap<>();
        body.put("name", "Gabe");
        body.put("response", "yes");
        body.put("event", String.format("http://localhost:%s/events/%s", port, event.getId()));

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

}
