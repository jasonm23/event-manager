package com.pinkpony.integration;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.pinkpony.model.CalendarEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class CancelCalendarEventTest extends PinkPonyIntegrationBase {

    String cancelUri;

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

        cancelUri = String.format("http://localhost:%d/cancelledEvents/%d", port, existingCalendarEvent.getId());
    }

    @After
    public void tearDown() {
        calendarEventRepository.deleteAll();
    }

    @Test
    public void cancelCalendarEvent(){
        String jsonInput = "{\"username\":\"Joe\"}";

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
    public void cancelCalendarEventWithWrongOrganiserKey(){
        String jsonInput = "{\"name\":\"Joe\"}";

        given().
                contentType(ContentType.JSON).
                body(jsonInput).
            when().
                patch(cancelUri).
            then().
                statusCode(400).
                body("cancelled", equalTo(false));
    }

    @Test
    public void cancelCalendarEventWithWrongOrganiserValue(){
        String jsonInput = "{\"username\":\"NotTheCalendarEventOrganizer\"}";

        given().
                contentType(ContentType.JSON).
                body(jsonInput).
                when().
                patch(cancelUri).
                then().
                statusCode(403).
                body("cancelled", equalTo(false)).
                body("username", equalTo("Joe"));

    }

    @Test
    public void cancelSameEventMultipleTimes() {
        String jsonInput = "{\"username\":\"Joe\"}";

        given().
                contentType(ContentType.JSON).
                body(jsonInput).
            when().
                patch(cancelUri).
            then().
                statusCode(200).
                body("cancelled", equalTo(true));

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
    public void cancelNonExistingEvent() {
        cancelUri = String.format("http://localhost:%d/cancelledEvents/%d", port, 200L);
        String jsonInput = "{\"username\":\"Joe\"}";

        given().
                contentType(ContentType.JSON).
                body(jsonInput).
            when().
                patch(cancelUri).
            then().
                statusCode(404);
    }
}
