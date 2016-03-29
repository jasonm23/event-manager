package com.pinkpony.service;

import com.pinkpony.model.CalendarEvent;
import com.pinkpony.model.CalendarEventMessageProjection;
import com.pinkpony.model.CalendarEventProjection;
import com.pinkpony.repository.CalendarEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CalendarEventService {

    @Autowired
    SpelAwareProxyProjectionFactory spelAwareProxyProjectionFactory;

    @Autowired
    CalendarEventRepository calendarEventRepository;

    public ResponseEntity<?> cancelEvent(Long eventId, Map<String, String> calendarEventMap) {
        CalendarEvent originalCalendarEvent = calendarEventRepository.findOne(eventId);

        if( originalCalendarEvent == null ) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }

        Resource<CalendarEvent> resource = new Resource<CalendarEvent>(originalCalendarEvent);

        if (calendarEventMap.get("username") == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resource);
        }

        if (! originalCalendarEvent.getUsername().equals(calendarEventMap.get("username"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resource);
        }

        if(! originalCalendarEvent.isCancelled()) {
            originalCalendarEvent.setCancelled(true);
            calendarEventRepository.save(originalCalendarEvent);
        }

        return ResponseEntity.ok(resource);
    }

    public ResponseEntity<?> createEvent(CalendarEvent calendarEvent, HttpRequest request) {

        //persist our data
        CalendarEvent savedEvent = calendarEventRepository.save(calendarEvent);

        //construct a project of our data
        CalendarEventMessageProjection calendarEventProjection = spelAwareProxyProjectionFactory.createProjection(CalendarEventMessageProjection.class, calendarEvent);

        //get the request accept header. inspect
        if(request.getHeaders().getAccept().contains(MediaType.APPLICATION_JSON)){

        }else if(request.getHeaders().getAccept().contains(MediaType.APPLICATION_JSON)){

        }

        //wrap our projection in a HateOS resource for response
        Resource<CalendarEventMessageProjection> calendarEventResource = new Resource<>(calendarEventProjection);
        return ResponseEntity.status(HttpStatus.CREATED).body(calendarEventResource);

    }
}
