package com.pinkpony.integration;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.pinkpony.model.CalendarEvent;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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
        calendarEventDate = (new DateTime(DateTimeZone.forID("UTC")).plusDays(1).toDate());

        existingCalendarEventInFuture = new CalendarEvent();
        existingCalendarEventInFuture.setName("BG Night");
        existingCalendarEventInFuture.setDescription("A Big Night of CalendarEventness");
        existingCalendarEventInFuture.setVenue("That amazing place");
        existingCalendarEventInFuture.setCalendarEventDateTime(calendarEventDate);
        existingCalendarEventInFuture.setCancelled(false);
        existingCalendarEventInFuture.setUsername("Joe");
        calendarEventRepository.save(existingCalendarEventInFuture);

        cancelUri = String.format("http://localhost:%d/calendarEvents/%d/cancel", port, existingCalendarEventInFuture.getId());
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
                statusCode(400);
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
                statusCode(403);

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
        cancelUri = String.format("http://localhost:%d/calendarEvents/%d/cancel", port, -1L);
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
