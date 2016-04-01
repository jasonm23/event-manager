package com.pinkpony.integration;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.pinkpony.PinkPonyApplication;
import com.pinkpony.model.CalendarEvent;
import com.pinkpony.repository.CalendarEventRepository;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PinkPonyApplication.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class UpcomingCalendarEventTest {

    @Autowired
    CalendarEventRepository calendarEventRepository;

    private final static DateFormat dateFormat = new SimpleDateFormat(CalendarEvent.FORMAT_STRING);
    static { dateFormat.setTimeZone(TimeZone.getTimeZone("UTC")); }

    @Value("${local.server.port}")
    int port;

    private DateTime today = new DateTime(DateTimeZone.forID("UTC"));
    private DateTime yesterday = today.minusDays(1);
    private DateTime tomorrow = today.plusDays(1);
    private DateTime nextWeek = today.plusWeeks(1);

    private String upcomingUrl = "/calendarEvents/search/upcomingEvents";

    @Before
    public void setUp() {
        calendarEventRepository.deleteAll();
        RestAssured.port = port;
    }

    private CalendarEvent makeCalendarEvent(Date date) {
        CalendarEvent newCalendarEvent = new CalendarEvent();
        newCalendarEvent.setName("Spring Boot Night");
        newCalendarEvent.setDescription("Wanna learn how to boot?");
        newCalendarEvent.setVenue("Arrowhead Lounge");
        newCalendarEvent.setCalendarEventDateTime(date);
        newCalendarEvent.setUsername("Holly");

        return newCalendarEvent;
    }

    /*
     * An upcoming event is one that:
     * - is in the future AND
     * - is not cancelled
     *
     * Upcoming events must be returned soonest first.
     */
    @Test
    public void getUpcomingEvents() {
        CalendarEvent pastEvent = makeCalendarEvent(yesterday.toDate());
        pastEvent.setName("past event");
        CalendarEvent futureEventTmr = makeCalendarEvent(tomorrow.toDate());
        futureEventTmr.setName("future event");
        CalendarEvent futureCancelledEvent = makeCalendarEvent(tomorrow.toDate());
        futureCancelledEvent.setName("future cancelled event");
        futureCancelledEvent.setCancelled(true);
        CalendarEvent futureEventNextWeek = makeCalendarEvent(nextWeek.toDate());
        futureEventNextWeek.setName("future event next week");

        calendarEventRepository.save(pastEvent);
        calendarEventRepository.save(futureEventNextWeek);
        calendarEventRepository.save(futureEventTmr);
        calendarEventRepository.save(futureCancelledEvent);

        given().
            contentType(ContentType.JSON).
        when().
            get(upcomingUrl).
        then().
            statusCode(200).
            body("_embedded.calendarEvents", hasSize(2)).
            body("_embedded.calendarEvents[0].name", equalTo("future event")).
            body("_embedded.calendarEvents[0].calendarEventDateTime", equalTo(dateFormat.format(tomorrow.toDate()))).
            body("_embedded.calendarEvents[1].name", equalTo("future event next week")).
            body("_embedded.calendarEvents[1].calendarEventDateTime", equalTo(dateFormat.format(nextWeek.toDate())));
    }
}
