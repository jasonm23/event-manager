package com.pinkpony.service;

import com.pinkpony.model.CalendarEvent;
import com.pinkpony.repository.CalendarEventRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventServiceTest {

    @Mock
    CalendarEventRepository eventRepository;

    @InjectMocks
    CalendarEventService eventService;

    CalendarEvent event;
    Map<String, String> eventData;

    @Before
    public void setup() {
        event = new CalendarEvent();
        event.setOrganizer("Frankel");
        event.setCancelled(false);

        eventData = new HashMap<>();
        eventData.put("organizer", "Frankel");
        eventData.put("cancelled", "true");

        when(eventRepository.findOne(event.getId())).thenReturn(event);
    }

    @Test
    public void testCancelEventSuccessfully() {
        ResponseEntity response = eventService.update(event.getId(), eventData);

        assertEquals(HttpStatus.OK,response.getStatusCode());

        assertTrue(response.getBody() instanceof Resource);
        CalendarEvent cancelledEvent = (CalendarEvent)((Resource)response.getBody()).getContent();
        assertEquals(true, cancelledEvent.isCancelled());
    }

    @Test
    public void testCancelEventWithWrongOrganizer() {
        eventData.put("organizer", "Lynwood");

        ResponseEntity response = eventService.update(event.getId(), eventData);

        assertEquals(HttpStatus.FORBIDDEN,response.getStatusCode());
        CalendarEvent cancelledEvent = (CalendarEvent)((Resource)response.getBody()).getContent();
        assertEquals(false, cancelledEvent.isCancelled());
    }

    @Test
    public void testCancelEventWithWrongCancelStatus() {
        eventData.put("cancelled", "random");

        ResponseEntity response = eventService.update(event.getId(), eventData);

        assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());
        CalendarEvent cancelledEvent = (CalendarEvent)((Resource)response.getBody()).getContent();
        assertEquals(false, cancelledEvent.isCancelled());
    }
}