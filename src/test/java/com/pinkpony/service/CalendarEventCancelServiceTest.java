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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CalendarEventCancelServiceTest {

    @Mock
    CalendarEventRepository eventRepository;

    @InjectMocks
    CalendarEventService eventService;

    CalendarEvent event;

    @Before
    public void setup() {
        event = new CalendarEvent();
        event.setUsername("Frankel");
        event.setCancelled(false);

        when(eventRepository.findOne(event.getId())).thenReturn(event);
    }

    @Test
    public void validCancelPersistsTheChange() {
        Boolean result = eventService.cancelEvent(event.getId(), event.getUsername());
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    public void validCancelReturnsTrue() {
        Boolean result = eventService.cancelEvent(event.getId(), event.getUsername());
        assertTrue(result);
    }

    @Test
    public void cantCancelWithWrongUsername() {
        Boolean result = eventService.cancelEvent(event.getId(), "wrongUser");
        assertFalse(result);
    }

    @Test
    public void cantCancelWithoutValidEventId() {
        Boolean result = eventService.cancelEvent(-1L, event.getUsername());
        assertFalse(result);
    }
}
