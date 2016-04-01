package com.pinkpony.integration;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.pinkpony.model.CalendarEvent;
import com.pinkpony.model.Rsvp;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;

import java.text.ParseException;
import java.util.Date;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class RsvpTest extends PinkPonyIntegrationBase {
    public String eventUri;

    CalendarEvent calendarEvent;
    String eventDateString = "2016-04-18T14:33:00+00:00";
    DateTime today = new DateTime(DateTimeZone.forID("UTC"));

    @Value("${local.server.port}")
    int port;

    @Before
    public void setUp() throws ParseException {
        RestAssured.port = port;
        calendarEventDate = dateFormat.parse(eventDateString);

        calendarEvent = new CalendarEvent();
        calendarEvent.setName("BG Night");
        calendarEvent.setDescription("A Big Night of CalendarEventness");
        calendarEvent.setVenue("That amazing place");
        calendarEvent.setCalendarEventDateTime(today.plusDays(1).toDate());
        calendarEvent.setUsername("Joe");
        calendarEventRepository.save(calendarEvent);

        eventUri = String.format("http://localhost:%s/calendarEvents/%s", port, calendarEvent.getId());
    }

    @Test
    public void createRsvp() throws Exception {
        String eventUri = String.format("http://localhost:%s/events/%s", port, calendarEvent.getId());

        JSONObject params = new JSONObject();
        params.put("username", "Gabe");
        params.put("response", "yes");
        params.put("calendarEvent", eventUri);

        given().
                contentType(ContentType.JSON).
                body(params.toString()).
        when().
                post("/rsvps").
        then().
                statusCode(201).
                body("_links.calendarEvent.href", containsString("/calendarEvent")).
                body("username", equalTo("Gabe")).
                body("response", equalTo("yes"));
    }

    @Test
    public void editRsvp() {
        Rsvp rsvp = createTestRsvp("Bobby", "yes");

        given().
                contentType(ContentType.JSON).
                request().body("{\"username\":\"Bobby\", \"response\":\"no\"}").
            when().
                patch(String.format("/rsvps/%s", rsvp.getId())).
            then().
                statusCode(200).
                body("response", equalTo("no")).
                body("username", equalTo("Bobby"));
    }

    @Test
    public void rsvpWithNoFields() throws Exception {
        JSONObject json = new JSONObject();
        json.put("username", "");
        json.put("response", "");
        //TODO: should we just test invalid post here? not all missing fields since this is duplicating the rsvpval unit tsts?

        given().
                contentType(ContentType.JSON).
                body(json.toString()).
            when().
                post(String.format("/rsvps")).
            then().
                statusCode(400).
                body("errors", hasSize(3)).
                body("errors[0].entity", equalTo("Rsvp")).
                body("errors[0].message", equalTo(messageSource.getMessage("rsvp.username.field.empty", null, LocaleContextHolder.getLocale()))).
                body("errors[0].property", equalTo("username")).
                body("errors[0].invalidValue", equalTo("")).
                body("errors[1].message", equalTo(messageSource.getMessage("rsvp.response.field.empty", null, LocaleContextHolder.getLocale()))).
                body("errors[1].property", equalTo("response")).
                body("errors[1].invalidValue", equalTo(""));

        //TODO: add more assertions about the shape and content of error messages in response
    }

    @Test
    public void creatRsvpToPastEventsShouldFail() throws JSONException {
        Date pastDate = today.minusDays(1).toDate();
        calendarEvent.setCalendarEventDateTime(pastDate);
        calendarEventRepository.save(calendarEvent);

        String eventUri = String.format("http://localhost:%s/events/%s", port, calendarEvent.getId());

        JSONObject params = new JSONObject();
        params.put("username", "Gabe");
        params.put("response", "yes");
        params.put("calendarEvent", eventUri);

        given().
                contentType(ContentType.JSON).
                body(params.toString()).
                when().
                post("/rsvps").
                then().
                statusCode(400).
                body("errors", hasSize(1)).
                body("errors[0].entity", equalTo("Rsvp")).
                body("errors[0].message", equalTo(messageSource.getMessage("rsvp.calendarEvent.field.createInThePast", null, LocaleContextHolder.getLocale()))).
                body("errors[0].property", equalTo("calendarEvent"));
    }

    @Test
    public void updateRsvpToPastEventsShouldFail() {
        Date pastDate = today.minusDays(1).toDate();

        calendarEvent.setCalendarEventDateTime(pastDate);
        Rsvp savedRsvp = new Rsvp();
        savedRsvp.setResponse("yes");
        savedRsvp.setUsername("Bobby");
        savedRsvp.calendarEvent = calendarEvent;

        rsvpRepository.save(savedRsvp);
        calendarEventRepository.save(calendarEvent);

        given().
                contentType(ContentType.JSON).
                request().body("{\"username\":\"Bobby\", \"response\":\"no\"}").
            when().
                patch(String.format("/rsvps/%s", savedRsvp.getId())).
            then().
                statusCode(400).
                body("errors", hasSize(1)).
                body("errors[0].entity", equalTo("Rsvp")).
                body("errors[0].message", equalTo(messageSource.getMessage("rsvp.calendarEvent.field.updateInThePast", null, LocaleContextHolder.getLocale()))).
                body("errors[0].property", equalTo("calendarEvent"));
    }
}
