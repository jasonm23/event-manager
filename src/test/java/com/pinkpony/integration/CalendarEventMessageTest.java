package com.pinkpony.integration;

import org.junit.Test;

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
                accept(AppConfig.MARVIN_JSON_MEDIATYPE_VALUE).
                contentType(ContentType.JSON).
                body(json.toString()).
        when().
                post("/calendarEvents?projection=eventMessage").
        then().log().all().
                statusCode(201).
                body("message", equalTo("event Bob's Blowout created")).
                body("message_type", equalTo("channel"));
    }
}
