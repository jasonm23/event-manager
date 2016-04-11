package com.pinkpony.integration;

import com.jayway.restassured.http.ContentType;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class CalendarEventMarvinUpdateTest extends PinkPonyIntegrationBase{

    private JSONObject marvinUpdateParams;
    private String marvinUpdateUrl = "/calendarEvents/update";

    private void setupParams() throws JSONException {
        marvinUpdateParams = new JSONObject();
        marvinUpdateParams.put("received_at","2016-04-11T08:27:19.867Z");
        marvinUpdateParams.put("channel","purplerobopony");
        marvinUpdateParams.put("attribute","name");
        marvinUpdateParams.put("id", existingCalendarEventInFuture.getId().toString());
        marvinUpdateParams.put("value","new name");
        marvinUpdateParams.put("command","event update 14 name=new");
        marvinUpdateParams.put("username",existingCalendarEventInFuture.getUsername());
    }

    @Test
    public void testUpdateCalendarEventSuccessfulWithMarvinParams() throws JSONException {
        setupParams();

        given().
            contentType(ContentType.JSON).
            body(marvinUpdateParams.toString()).
        when().
            patch(marvinUpdateUrl).
        then().
            statusCode(200).
            body("id", equalTo(existingCalendarEventInFuture.getId().intValue())).
            body("name", equalTo("new name")).
            body("description", equalTo(existingCalendarEventInFuture.getDescription())).
            body("venue", equalTo(existingCalendarEventInFuture.getVenue())).
            body("calendarEventDateTime", equalTo(existingCalendarEventInFuture.getFormattedEventDateTime()));
    }

    @Test
    public void testUpdateCalendarEventFailWithEmptyAttribute() throws JSONException {
        setupParams();
        marvinUpdateParams.remove("attribute");

        given().
            contentType(ContentType.JSON).
            body(marvinUpdateParams.toString()).
        when().
            patch(marvinUpdateUrl).
        then().log().all().
            statusCode(400).
            body("errors", hasSize(1)).

            body("errors[0].entity", equalTo("CalendarEvent")).
            body("errors[0].message", equalTo(messageSource.getMessage("marvinUpdateMap.attribute.field.empty", null, LocaleContextHolder.getLocale()))).
            body("errors[0].property", equalTo("attribute")).
            body("errors[0].invalidValue", equalTo("null"));
    }
}
