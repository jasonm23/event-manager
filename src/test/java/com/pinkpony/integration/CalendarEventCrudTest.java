package com.pinkpony.integration;

import com.jayway.restassured.http.ContentType;
import com.pinkpony.model.CalendarEvent;
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
            body("calendarEventDateTime", equalTo(calendarEventDateString)).
            body("username", equalTo("Joe"));
    }

    @Test
    public void listRsvpsForAnEvent() {
        // When an event has RSVPs...
        createTestRsvp("yes", "Billy");
        createTestRsvp("yes", "Sarah");
        createTestRsvp("no", "Jo");
        createTestRsvp("yes", "Colin");
        createTestRsvp("no", "Trudy");
        createTestRsvp("no", "Heng");

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

    @Test
    public void viewEventDetailsIncludingRsvpsViaProjection() {
        CalendarEvent event = existingCalendarEvent;
        event = addRsvp(event, "yes", "Ron");
        event = addRsvp(event, "no", "Hermione");

        given().
            contentType(ContentType.JSON).
        when().
            get(String.format("/calendarEvents/%s?projection=inlineRsvps", event.getId())).
        then().
            statusCode(200).
            body("name", equalTo("Spring Boot Night")).
            body("description", equalTo("Wanna learn how to boot?")).
            body("venue", equalTo("Arrowhead Lounge")).
            body("calendarEventDateTime", equalTo(calendarEventDateString)).
            body("username", equalTo("Holly")).
            body("rsvps[0].username", equalTo("Ron")).
            body("rsvps[0].response", equalTo("yes")).
            body("rsvps[1].username", equalTo("Hermione")).
            body("rsvps[1].response", equalTo("no"));
    }

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
