package com.pinkpony.service;

import com.pinkpony.model.CalendarEvent;
import com.pinkpony.repository.CalendarEventRepository;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.data.rest.webmvc.support.RepositoryConstraintViolationExceptionMessage;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CalendarEventMarvinPatchServiceTest {
    @Mock
    CalendarEventRepository eventRepository;

    @Mock
    MessageSource messageSource;

    @InjectMocks
    CalendarEventService eventService;

    CalendarEvent event;

    private Map<String, String> marvinUpdateParams;

    @Before
    public void setup() {
        Date date = new DateTime().plusDays(2).toDate();
        event = new CalendarEvent();
        event.setId(1L);
        event.setName("Spring Boot Night");
        event.setDescription("Wanna learn how to boot?");
        event.setVenue("Arrowhead Lounge");
        event.setCalendarEventDateTime(date);
        event.setCalendarEventDateTimeString(CalendarEvent.dateFormat.format(date));
        event.setUsername("Holly");


        when(eventRepository.findOne(event.getId())).thenReturn(event);

        marvinUpdateParams = new HashMap<>();
        marvinUpdateParams.put("received_at","2016-04-11T08:27:19.867Z");
        marvinUpdateParams.put("channel","purplerobopony");
        marvinUpdateParams.put("attribute","name");
        marvinUpdateParams.put("id", event.getId().toString());
        marvinUpdateParams.put("value","new name");
        marvinUpdateParams.put("command","event update 14 name=new");
        marvinUpdateParams.put("username","Holly");
    }

    @Test
    public void marvinUpdateEventWithCorrectParamsShouldSucceed() {
        ResponseEntity<ResourceSupport> response = eventService.marvinUpdateEvent(marvinUpdateParams);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Resource resource = (Resource)response.getBody();
        assertTrue(resource.getContent() instanceof CalendarEvent);
        CalendarEvent updateCalendarEvent = (CalendarEvent) resource.getContent();
        assertEquals("new name", updateCalendarEvent.getName());
        assertEquals(event.getDescription(), updateCalendarEvent.getDescription());
        assertEquals(event.getVenue(), updateCalendarEvent.getVenue());
        assertEquals(event.getCalendarEventDateTimeString(), updateCalendarEvent.getCalendarEventDateTimeString());
        assertEquals(event.getUsername(), updateCalendarEvent.getUsername());
    }

    @Test
    public void marvinUpdateEventWithEmptyFieldsShouldFail() {
        marvinUpdateParams.remove("id");
        marvinUpdateParams.remove("attribute");
        marvinUpdateParams.remove("value");

        ResponseEntity<ResourceSupport> response = eventService.marvinUpdateEvent(marvinUpdateParams);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Resource resource = (Resource)response.getBody();
        assertTrue(resource.getContent() instanceof RepositoryConstraintViolationExceptionMessage);
        RepositoryConstraintViolationExceptionMessage errorMessages = (RepositoryConstraintViolationExceptionMessage) resource.getContent();
        assertEquals(3, errorMessages.getErrors().size());
        assertEquals("id", errorMessages.getErrors().get(0).getProperty());
        assertEquals("attribute", errorMessages.getErrors().get(1).getProperty());
        assertEquals("value", errorMessages.getErrors().get(2).getProperty());
    }
}
