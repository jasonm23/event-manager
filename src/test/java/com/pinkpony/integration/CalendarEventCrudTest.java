package com.pinkpony.integration;

import com.jayway.restassured.http.ContentType;
import com.pinkpony.model.CalendarEvent;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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

        String formattedDateString = dateFormat.format(dateFormat.parse(calendarEventDateString));

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
            body("calendarEventDateTime", equalTo(formattedDateString)).
            body("username", equalTo("Joe")).
            body(not(hasItem("message"))).
            body(not(hasItem("message_type")));
    }

    @Test
    public void getCalendarEvent() {

        given().
            accept("application/json").
            contentType(ContentType.JSON).
        when().
            get(String.format("/calendarEvents/%d", existingCalendarEventInFuture.getId())).
        then().
            statusCode(200).
            body("name", equalTo(existingCalendarEventInFuture.getName())).
            body("description", equalTo(existingCalendarEventInFuture.getDescription())).
            body("venue", equalTo(existingCalendarEventInFuture.getVenue())).
            body("username", equalTo(existingCalendarEventInFuture.getUsername())).
            body("calendarEventDateTime", equalTo(existingCalendarEventInFuture.getCalendarEventDateTimeString())).
            body(not(hasItem("message"))).
            body(not(hasItem("message_type")));
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
            get(String.format("/calendarEvents/%s/rsvps", existingCalendarEventInFuture.getId())).
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
    public void viewEventsShowBasicInformation() {
        CalendarEvent secondEvent = calendarEventRepository.save(makeCalendarEvent(calendarEventDate));

        given().
                contentType(ContentType.JSON).
            when().
                get(String.format("/calendarEvents")).
            then().
                statusCode(200).
                body("_embedded.calendarEvents", hasSize(2)).
                body("_embedded.calendarEvents[0].name", equalTo(existingCalendarEventInFuture.getName())).
                body("_embedded.calendarEvents[0].description", equalTo(existingCalendarEventInFuture.getDescription())).
                body("_embedded.calendarEvents[0].venue", equalTo(existingCalendarEventInFuture.getVenue())).
                body("_embedded.calendarEvents[0].calendarEventDateTime", equalTo(existingCalendarEventInFuture.getCalendarEventDateTimeString())).
                body("_embedded.calendarEvents[0].username", equalTo(existingCalendarEventInFuture.getUsername())).
                body("_embedded.calendarEvents[0].rsvps", equalTo(null)).
                body("_embedded.calendarEvents[1].name", equalTo(secondEvent.getName())).
                body("_embedded.calendarEvents[1].description", equalTo(secondEvent.getDescription())).
                body("_embedded.calendarEvents[1].venue", equalTo(secondEvent.getVenue())).
                body("_embedded.calendarEvents[1].calendarEventDateTime", equalTo(secondEvent.getCalendarEventDateTimeString())).
                body("_embedded.calendarEvents[1].username", equalTo(secondEvent.getUsername())).
                body("_embedded.calendarEvents[1].rsvps", equalTo(null));
    }

    @Test
    public void viewEventDetailsIncludingRsvpsViaProjection() {
        CalendarEvent event = existingCalendarEventInFuture;
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

    @Test
    public void updateUsernameShouldFail() throws Exception {

        JSONObject params = new JSONObject();
        params.put("username", "Joe");
        String putUri = String.format("/calendarEvents/%d", existingCalendarEventInFuture.getId());

        given().
                contentType(ContentType.JSON).
                body(params.toString()).
            when().
                patch(putUri).
            then().
                statusCode(400).
                body("errors[0].entity", equalTo("CalendarEvent")).
                body("errors[0].message", equalTo(messageSource.getMessage("calendarEvent.username.field.mismatch", null, LocaleContextHolder.getLocale())));
        ;

    }

    @Test
    public void updateFieldsAfterEventStartsShouldFail() throws Exception {

        JSONObject params = new JSONObject();
        DateTime today = new DateTime(DateTimeZone.UTC);
        CalendarEvent existingCalEvent = makePastCalendarEvent();
        calendarEventRepository.save(existingCalEvent);

        params.put("name", "some other name");
        String patchUri = String.format("/calendarEvents/%d", existingCalEvent.getId());

        given().
                contentType(ContentType.JSON).
                body(params.toString()).
            when().
                patch(patchUri).
            then().
                statusCode(400).
                body("errors[0].entity", equalTo("CalendarEvent")).
                body("errors[0].message", equalTo(messageSource.getMessage("calendarEvent.calendarEventDateTime.field.eventHasAlreadyStarted", null, LocaleContextHolder.getLocale())));
    }

    @Test
    public void disallowPUTMethodForUpdateEvent() {

        JSONObject params = new JSONObject();
        String putUri = String.format("/calendarEvents/%d", existingCalendarEventInFuture.getId());

        given().
                contentType(ContentType.JSON).
                body(params.toString()).
            when().
                put(putUri).
            then().
                statusCode(405);
    }

    @Test
    public void testPatchUpdateEventDateTimeInPastIsInvalid() throws Exception {
        String newDateTimeString = CalendarEvent.dateFormat.format(DateTime.now().minusDays(2).toDate());
        JSONObject params = new JSONObject();
        params.put("calendarEventDateTime", newDateTimeString);
        params.put("username", existingCalendarEventInFuture.getUsername());

        String patchUri = String.format("/calendarEvents/%d", existingCalendarEventInFuture.getId());

        given().
                contentType(ContentType.JSON).
                body(params.toString()).
            when().
                patch(patchUri).
            then().
                statusCode(400).
                body("errors[0].message", equalTo(messageSource.getMessage("calendarEvent.calendarEventDateTime.field.cantSetDateInPast", null, LocaleContextHolder.getLocale())));

    }

    @Test
    public void patchEventDateTimeWithFutureTimeSucceeds() throws Exception {
        String newDateTimeString = CalendarEvent.dateFormat.format(DateTime.now().plusDays(2).toDate());
        JSONObject params = new JSONObject();
        params.put("calendarEventDateTime", newDateTimeString);
        params.put("username", existingCalendarEventInFuture.getUsername());

        String patchUri = String.format("/calendarEvents/%d", existingCalendarEventInFuture.getId());

        given().
                contentType(ContentType.JSON).
                body(params.toString()).
                when().
                patch(patchUri).
                then().
                statusCode(200).
                body("calendarEventDateTime", equalTo(newDateTimeString));
    }

    @Test
    public void updatingEventAttributeDescribesSuccess() throws Exception {
        JSONObject params = new JSONObject();
        params.put("username", existingCalendarEventInFuture.getUsername());
        params.put("name", "New Event Name");
        String uri = String.format("/calendarEvents/%d", existingCalendarEventInFuture.getId());

        given().
            contentType(ContentType.JSON).
            body(params.toString()).
        when().
            patch(uri).
        then().log().all().
            statusCode(200).
            body("id", equalTo(existingCalendarEventInFuture.getId().intValue())).
            body("name", equalTo("New Event Name")).
            body("description", equalTo(existingCalendarEventInFuture.getDescription())).
            body("venue", equalTo(existingCalendarEventInFuture.getVenue())).
            body("calendarEventDateTime", equalTo(existingCalendarEventInFuture.getFormattedEventDateTime()));
    }
}
