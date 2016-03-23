package com.pinkpony.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.pinkpony.PinkPonyApplication;
import com.pinkpony.model.Event;
import com.pinkpony.model.Rsvp;
import com.pinkpony.repository.EventRepository;
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

import java.text.ParseException;
import java.util.HashMap;

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
    EventRepository eventRepository;

    @Autowired
    MessageSource messageSource;

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

        eventUri = String.format("http://localhost:%s/events/%s", port, event.getId());
    }

    @Test
    public void rsvpWithNoFields() throws JsonProcessingException, ParseException {
        ObjectMapper  mapper = new ObjectMapper();

        Rsvp body = new Rsvp();
        body.setName("");
        body.setResponse("");
        //TODO: should we just test invalid post here? not all missing fields since this is duplicating the rsvpval unit tsts?

        given().log().all().
                contentType(ContentType.JSON).
                body(mapper.writeValueAsString(body)).
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

        //TODO: add more assertons about the shape and content of error messages in response
    }
}
