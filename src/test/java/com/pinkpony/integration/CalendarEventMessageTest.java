package com.pinkpony.integration;

import com.jayway.restassured.http.ContentType;
import com.pinkpony.config.AppConfig;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

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
