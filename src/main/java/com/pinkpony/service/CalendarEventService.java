package com.pinkpony.service;

import com.pinkpony.model.CalendarEvent;
import com.pinkpony.repository.CalendarEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CalendarEventService {

    @Autowired
    CalendarEventRepository eventRepository;

    public ResponseEntity<?> handleCalendarEventPUT(Long eventId, Map<String, String> eventMap) {
        CalendarEvent originalCalendarEvent = eventRepository.findOne(eventId);
        Resource<CalendarEvent> resource = new Resource<CalendarEvent>(originalCalendarEvent);

        if (! originalCalendarEvent.getUsername().equals(eventMap.get("username"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resource);
        }

        String cancellationStatus = eventMap.get("cancelled").toLowerCase();
        if (! (cancellationStatus.equals("false") || cancellationStatus.equals("true"))){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resource);
        }

        originalCalendarEvent.setCancelled(Boolean.parseBoolean(eventMap.get("cancelled")));
        eventRepository.save(originalCalendarEvent);
        return ResponseEntity.ok(resource);
    }


}
