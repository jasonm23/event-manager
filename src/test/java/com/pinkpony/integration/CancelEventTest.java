package com.pinkpony.integration;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.pinkpony.PinkPonyApplication;
import com.pinkpony.model.Event;
import com.pinkpony.repository.EventRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PinkPonyApplication.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class CancelEventTest {


    @Autowired
    EventRepository eventRepository;

    private final static DateFormat dateFormat = new SimpleDateFormat(Event.FORMAT_STRING);
    String eventDateString = "2016-03-18T14:33:00+0000";
    Event existingEvent;
    Date eventDate;

    @Value("${local.server.port}")
    int port;

    @Before
    public void setUp() throws ParseException {
        RestAssured.port = port;
        eventDate = dateFormat.parse(eventDateString);
        existingEvent = new Event();
        existingEvent.setName("BG Night");
        existingEvent.setDescription("A Big Night of Eventness");
        existingEvent.setVenue("That amazing place");
        existingEvent.setEventDateTime(eventDate);
        existingEvent.setCancelled(false);
        existingEvent.setOrganizer("Joe");
        eventRepository.save(existingEvent);
    }

    @After
    public void tearDown() {
        eventRepository.deleteAll();
    }

    @Test
    public void cancelEvent(){

        String cancelUri = String.format("http://localhost:%d/events/%d", port, existingEvent.getId());
        String jsonInput = "{\"organizer\":\"Joe\", \"cancelled\":\"true\"}";

        given().
                contentType(ContentType.JSON).
                body(jsonInput).
            when().
                patch(cancelUri).
            then().
                statusCode(200).
                body("cancelled", equalTo(true));

    }

    @Test
    public void cancelEventWithWrongOrganiser(){

        String cancelUri = String.format("http://localhost:%d/events/%d", port, existingEvent.getId());
        String jsonInput = "{\"organizer\":\"NotTheEventOrganizer\", \"cancelled\":\"true\"}";

        given().
                contentType(ContentType.JSON).
                body(jsonInput).
           when().
                patch(cancelUri).
           then().
                statusCode(403).
                body("cancelled", equalTo(false));

    }

    @Test
    public void cancelEventWithNonBoolean(){

        String cancelUri = String.format("http://localhost:%d/events/%d", port, existingEvent.getId());
        String jsonInput = "{\"organizer\":\"Joe\", \"cancelled\":\"booptieboo\"}";

        given().
                contentType(ContentType.JSON).
                body(jsonInput).
           when().
                patch(cancelUri).
           then().
                statusCode(400);

    }
}
