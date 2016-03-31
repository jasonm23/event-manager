package com.pinkpony.integration;

import com.jayway.restassured.http.ContentType;
import com.pinkpony.config.MarvinMediaTypes;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsNot.not;

public class CalendarEventMessageTest extends PinkPonyIntegrationBase {

    @Test
    public void calendarEventMessageProjection() throws JSONException {
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
        then().log().all().
                statusCode(201).
                body("message", equalTo("event Bob's Blowout created")).
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

        given().
                accept(MarvinMediaTypes.MARVIN_JSON_MEDIATYPE_VALUE).
                contentType(ContentType.JSON).
                body(json.toString()).
        when().
                post("/calendarEvents").
        then().log().all().
                statusCode(201).
                body("name", equalTo("Spring Boot Night")).
                body("description", equalTo("A Big Night of CalendarEventness")).
                body("venue", equalTo("Arrowhead Lounge")).
                body("calendarEventDateTime", equalTo(calendarEventDateString)).
                body("username", equalTo("Joe")).
                body("message", equalTo("event "+ json.get("name")+" created")).
                body("message_type", equalTo("channel"));
    }
}
