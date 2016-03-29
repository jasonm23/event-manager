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
    CalendarEventRepository calendarEventRepository;

    public ResponseEntity<?> cancelEvent(Long eventId, Map<String, String> eventMap) {
        CalendarEvent originalCalendarEvent = calendarEventRepository.findOne(eventId);

        if( originalCalendarEvent == null ) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }

        Resource<CalendarEvent> resource = new Resource<CalendarEvent>(originalCalendarEvent);

        if (eventMap.get("username") == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resource);
        }

        if (! originalCalendarEvent.getUsername().equals(eventMap.get("username"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resource);
        }

        if(! originalCalendarEvent.isCancelled()) {
            originalCalendarEvent.setCancelled(true);
            calendarEventRepository.save(originalCalendarEvent);
        }

        return ResponseEntity.ok(resource);
    }


}
