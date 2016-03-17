package com.pinkpony.integration;

import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.pinkpony.PinkPonyApplication;
import com.pinkpony.model.Event;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.CORBA.Object;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PinkPonyApplication.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")

public class EventCrudTest {

    @Value("${local.server.port}")
    int port;

    @Before
    public void setUp() {
        //RestAssured.port = port;
    }

    @Test
    public void createEvent() throws JsonProcessingException, ParseException {
        ObjectMapper  mapper = new ObjectMapper();

        Event event = new Event();
        event.setName("BG Night");
        event.setDescription("A Big Night of Eventness");
        event.setVenue("That amazing place");
        event.setEventDateTime("2016-03-18 14:33:00");
        event.setOrganizer("Joe");

        given().port(port).log().all().
                contentType(ContentType.JSON).
            body(mapper.writeValueAsString(event)).
        when().
            post("/events").
        then().
            statusCode(201).
            body("name", equalTo("BG Night")).
            body("description", equalTo("A Big Night of Eventness")).
            body("venue", equalTo("That amazing place")).
            body("eventDateTime", equalTo("2016-03-18 14:33:00")).
            body("organizer", equalTo("Joe"));
    }

}
