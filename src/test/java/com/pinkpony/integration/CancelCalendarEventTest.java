package com.pinkpony.integration;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.pinkpony.PinkPonyApplication;
import com.pinkpony.model.CalendarEvent;
import com.pinkpony.repository.CalendarEventRepository;
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
public class CancelCalendarEventTest {


    @Autowired
    CalendarEventRepository calendarEventRepository;

    private final static DateFormat dateFormat = new SimpleDateFormat(CalendarEvent.FORMAT_STRING);
    String calendarEventDateString = "2016-03-18T14:33:00+0000";
    CalendarEvent existingCalendarEvent;
    Date calendarEventDate;

    @Value("${local.server.port}")
    int port;

    @Before
    public void setUp() throws ParseException {
        RestAssured.port = port;
        calendarEventDate = dateFormat.parse(calendarEventDateString);
        existingCalendarEvent = new CalendarEvent();
        existingCalendarEvent.setName("BG Night");
        existingCalendarEvent.setDescription("A Big Night of CalendarEventness");
        existingCalendarEvent.setVenue("That amazing place");
        existingCalendarEvent.setCalendarEventDateTime(calendarEventDate);
        existingCalendarEvent.setCancelled(false);
        existingCalendarEvent.setUsername("Joe");
        calendarEventRepository.save(existingCalendarEvent);
    }

    @After
    public void tearDown() {
        calendarEventRepository.deleteAll();
    }

    @Test
    public void cancelCalendarEvent(){

        String cancelUri = String.format("http://localhost:%d/calendarEvents/%d", port, existingCalendarEvent.getId());
        String jsonInput = "{\"username\":\"Joe\", \"cancelled\":\"true\"}";

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
    public void cancelCalendarEventWithWrongOrganiser(){

        String cancelUri = String.format("http://localhost:%d/calendarEvents/%d", port, existingCalendarEvent.getId());
        String jsonInput = "{\"username\":\"NotTheCalendarEventOrganizer\", \"cancelled\":\"true\"}";

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
    public void cancelCalendarEventWithNonBoolean(){

        String cancelUri = String.format("http://localhost:%d/calendarEvents/%d", port, existingCalendarEvent.getId());
        String jsonInput = "{\"username\":\"Joe\", \"cancelled\":\"booptieboo\"}";

        given().
                contentType(ContentType.JSON).
                body(jsonInput).
           when().
                patch(cancelUri).
           then().
                statusCode(400);

    }
}
