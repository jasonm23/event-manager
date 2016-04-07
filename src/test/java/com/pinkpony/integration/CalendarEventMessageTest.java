package com.pinkpony.integration;

import com.jayway.restassured.http.ContentType;
import com.pinkpony.config.MarvinMediaTypes;
import com.pinkpony.model.Rsvp;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsNot.not;

public class CalendarEventMessageTest extends PinkPonyIntegrationBase {

    @Test
    public void calendarEventMessageProjection() throws JSONException {
        String messageString = String.format("# Bob's Blowout\\\\n\\\\n*Big party for Uncle Bob*\\\\nAt %s\\\\n\\\\n## Attendees:\\\\n{rsvps:\\\\n}", calendarEventDateString);

        JSONObject json = new JSONObject();
        json.put("name","Bob's Blowout");
        json.put("description","Big party for Uncle Bob");
        json.put("username","Joe Smoove");
        json.put("calendarEventDateTime", calendarEventDateString);
        json.put("venue","Da Hacienda");

        given().
                accept(MarvinMediaTypes.MARVIN_JSON_MEDIATYPE_VALUE).
                contentType(ContentType.JSON).
                body(json.toString()).
        when().
                post("/calendarEvents?projection=eventMessage").
        then().
                statusCode(201).
                body("message", equalTo(messageString)).
                body("message_type", equalTo("channel"));
    }

    @Test
    public void createCalendarEventWithGenericAcceptHeader() throws Exception {

        JSONObject json = new JSONObject();
        json.put("name", "Spring Boot Night");
        json.put("calendarEventDateTime", calendarEventDateString);
        json.put("description", "A Big Night of CalendarEventness");
        json.put("username", "Joe");
        json.put("venue", "Arrowhead Lounge");

        given().
                header("ACCEPT" , "application/json").
                contentType(ContentType.JSON).
                body(json.toString()).
        when().
                post("/calendarEvents").
        then().
                statusCode(201).
                body("name", equalTo("Spring Boot Night")).
                body(not(hasItem("message"))).
                body(not(hasItem("message_type")));
    }

    @Test
    public void createBadCalendarEventWithMarvinAcceptHeader() throws Exception {
        JSONObject json = new JSONObject();
        json.put("description", "A Big Night of CalendarEventness");
        json.put("username", "Joe");
        json.put("venue", "Arrowhead Lounge");

        given().
                accept(MarvinMediaTypes.MARVIN_JSON_MEDIATYPE_VALUE).
                contentType(ContentType.JSON).
                body(json.toString()).
        when().
                post("/calendarEvents").
        then().
                statusCode(400).
                body("errors", hasSize(2)).
                body("errors[0].entity", equalTo("CalendarEvent")).
                body("errors[0].message", equalTo(messageSource.getMessage("calendarEvent.name.field.empty", null, LocaleContextHolder.getLocale()))).
                body("errors[0].property", equalTo("name")).
                body("errors[1].entity", equalTo("CalendarEvent")).
                body("errors[1].message", equalTo(messageSource.getMessage("calendarEvent.calendarEventDateTime.field.empty", null, LocaleContextHolder.getLocale()))).
                body("errors[1].property", equalTo("calendarEventDateTimeString"));
    }

    @Test
    public void createCalendarEventWithMarvinAcceptHeader() throws Exception {

        JSONObject json = new JSONObject();
        json.put("name", "Spring Boot Night");
        json.put("calendarEventDateTime", calendarEventDateString);
        json.put("description", "A Big Night of CalendarEventness");
        json.put("username", "Joe");
        json.put("venue", "Arrowhead Lounge");
        String messageString = String.format("# Spring Boot Night\\\\n\\\\n*A Big Night of CalendarEventness*\\\\nAt %s\\\\n\\\\n## Attendees:\\\\n{rsvps:\\\\n}", calendarEventDateString);

        given().
                accept(MarvinMediaTypes.MARVIN_JSON_MEDIATYPE_VALUE).
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
                body("username", equalTo("Joe")).
                body("message", equalTo(messageString)).
                body("message_type", equalTo("channel"));
    }

    // TODO: do the same for get event
    @Test
    public void getCalendarEventWithRsvpWithMarvinAcceptHeader() {
        String messageString = String.format("# Spring Boot Night\\\\n\\\\n*Wanna learn how to boot?*\\\\nAt %s\\\\n\\\\n## Attendees:\\\\n{rsvps:\\\\n}", calendarEventDateString);

        existingCalendarEvent = addRsvp(existingCalendarEvent, "yes", "Yifeng");

        given().
                accept(MarvinMediaTypes.MARVIN_JSON_MEDIATYPE_VALUE).
                contentType(ContentType.JSON).
                when().
                get(String.format("/calendarEvents/%d", existingCalendarEvent.getId())).
                then().
                statusCode(200).
                body("message", equalTo(messageString)).
                body("message_type", equalTo("channel")).
                body("rsvps", hasSize(1)).
                body("rsvps[0].message", equalTo("Yifeng, yes"));
    }
}
