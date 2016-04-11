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
public class CalendarEventPatchServiceTest {
    @Mock
    CalendarEventRepository eventRepository;

    @Mock
    MessageSource messageSource;

    @InjectMocks
    CalendarEventService eventService;

    CalendarEvent event;
    Map<String, String> eventData;

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

        eventData = new HashMap<>();

        when(eventRepository.findOne(event.getId())).thenReturn(event);
    }

    @Test
    public void patchEventWithInvalidParams() {
        eventData.put("name", "");
        eventData.put("calendarEventDateTimeString", "2016-01-01 11:22:33+08:00");

        ResponseEntity<ResourceSupport> response = eventService.patchEvent(event.getId(), eventData);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Resource resource = (Resource)response.getBody();
        assertTrue(resource.getContent() instanceof RepositoryConstraintViolationExceptionMessage);
        RepositoryConstraintViolationExceptionMessage errorMessages = (RepositoryConstraintViolationExceptionMessage) resource.getContent();
        assertEquals(2, errorMessages.getErrors().size());
        assertEquals("name", errorMessages.getErrors().get(0).getProperty());
        assertEquals("calendarEventDateTimeString", errorMessages.getErrors().get(1).getProperty());
    }

    @Test
    public void patchToPastEventShouldFail() {
        eventData.put("name", "new name");
        Date yesterday = new DateTime().minusDays(1).toDate();
        event.setCalendarEventDateTimeString(CalendarEvent.dateFormat.format(yesterday));
        event.setCalendarEventDateTime(yesterday);

        ResponseEntity<ResourceSupport> response = eventService.patchEvent(event.getId(), eventData);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Resource resource = (Resource)response.getBody();
        assertTrue(resource.getContent() instanceof RepositoryConstraintViolationExceptionMessage);
        RepositoryConstraintViolationExceptionMessage errorMessages = (RepositoryConstraintViolationExceptionMessage) resource.getContent();
        assertEquals(1, errorMessages.getErrors().size());
        assertEquals("calendarEventDateTime", errorMessages.getErrors().get(0).getProperty());
    }

    @Test
    public void patchToUpdateUsernameShouldFail() {
        eventData.put("username", "Another Username");

        ResponseEntity<ResourceSupport> response = eventService.patchEvent(event.getId(), eventData);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Resource resource = (Resource)response.getBody();
        assertTrue(resource.getContent() instanceof RepositoryConstraintViolationExceptionMessage);
        RepositoryConstraintViolationExceptionMessage errorMessages = (RepositoryConstraintViolationExceptionMessage) resource.getContent();
        assertEquals(1, errorMessages.getErrors().size());
        assertEquals("username", errorMessages.getErrors().get(0).getProperty());
    }

    @Test
    public void patchSuccessfully() {
        eventData.put("name", "new name");
        eventData.put("venue", "new venue");
        eventData.put("description", "new description");

        ResponseEntity<ResourceSupport> response = eventService.patchEvent(event.getId(), eventData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Resource resource = (Resource)response.getBody();
        assertTrue(resource.getContent() instanceof CalendarEvent);

    }
}
