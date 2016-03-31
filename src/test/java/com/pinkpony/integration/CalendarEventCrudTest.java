package com.pinkpony.integration;

import com.jayway.restassured.http.ContentType;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CalendarEventCrudTest extends PinkPonyIntegrationBase {

    @Test
    public void createCalendarEvent() throws Exception {
        JSONObject json = new JSONObject();
        json.put("name", "Spring Boot Night");
        json.put("calendarEventDateTime", calendarEventDateString);
        json.put("description", "A Big Night of CalendarEventness");
        json.put("username", "Joe");
        json.put("venue", "Arrowhead Lounge");

        given().
            accept("application/json").
            contentType(ContentType.JSON).
            body(json.toString()).
        when().
            post("/calendarEvents").
        then().
            statusCode(201).
            body("name", equalTo("Spring Boot Night")).
            body("description", equalTo("A Big Night of CalendarEventness")).
            body("venue", equalTo("Arrowhead Lounge")).
            body("username", equalTo("Joe"));
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
            get(String.format("/calendarEvents/%s/rsvps", existingCalendarEvent.getId())).
        then().
            statusCode(200).
            body("_embedded.rsvps[0].username", containsString("Billy")).
            body("_embedded.rsvps[0].response", containsString("yes")).
            body("_embedded.rsvps[1].username", containsString("Sarah")).
            body("_embedded.rsvps[1].response", containsString("yes")).
            body("_embedded.rsvps[2].username", containsString("Jo")).
            body("_embedded.rsvps[2].response", containsString("no")).
            body("_embedded.rsvps[3].username", containsString("Colin")).
            body("_embedded.rsvps[3].response", containsString("yes")).
            body("_embedded.rsvps[4].username", containsString("Trudy")).
            body("_embedded.rsvps[4].response", containsString("no")).
            body("_embedded.rsvps[5].username", containsString("Heng")).
            body("_embedded.rsvps[5].response", containsString("no"));
    }

//   SF>: This test belongs to a future story. See #116011543
//        This breaks current patch behaviour
//    @Test
//    public void editCalendarEvent() {
//        given().
//            contentType(ContentType.JSON).
//            request().body("{\"name\":\"Mah CalendarEvent Name is Changed\"}").
//        when().
//            patch(String.format("/calendarEvents/%s", existingCalendarEvent.getId())).
//        then().
//            statusCode(200).
//            body("name", equalTo("Mah CalendarEvent Name is Changed"));
//    }

    @Test
    public void badRequestOnErrors() throws Exception {
        JSONObject params = new JSONObject();
        params.put("description", "A Big Night of Eventness");
        params.put("calendarEventDateTime", "not a date");
        params.put("username", "Joe");
        params.put("venue", "Arrowhead Lounge");

        given().
                contentType(ContentType.JSON).
                body(params.toString()).
        when().
                post("/calendarEvents").
        then().
                statusCode(400).
                body("errors", hasSize(2)).

                body("errors[0].entity", equalTo("CalendarEvent")).
                body("errors[0].message", equalTo(messageSource.getMessage("calendarEvent.name.field.empty", null, LocaleContextHolder.getLocale()))).
                body("errors[0].property", equalTo("name")).
                body("errors[0].invalidValue", equalTo("null")).

                body("errors[1].entity", equalTo("CalendarEvent")).
                body("errors[1].message", equalTo(messageSource.getMessage("calendarEvent.calendarEventDateTime.field.invalid", null, LocaleContextHolder.getLocale()))).
                body("errors[1].property", equalTo("calendarEventDateTimeString")).
                body("errors[1].invalidValue", equalTo("not a date"));
    }
}
