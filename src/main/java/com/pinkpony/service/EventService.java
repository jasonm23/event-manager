package com.pinkpony.service;


import com.pinkpony.model.Event;
import com.pinkpony.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EventService {

    @Autowired
    EventRepository eventRepository;

    public ResponseEntity<?> handleEventPUT(Long eventId, Map<String, String> eventMap) {
        Event originalEvent = eventRepository.findOne(eventId);
        Resource<Event> resource = new Resource<Event>(originalEvent);

        if (! originalEvent.getOrganizer().equals(eventMap.get("organizer"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resource);
        }

        String cancellationStatus = eventMap.get("cancelled").toLowerCase();
        if (! (cancellationStatus.equals("false") || cancellationStatus.equals("true"))){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resource);
        }

        originalEvent.setCancelled(Boolean.parseBoolean(eventMap.get("cancelled")));
        eventRepository.save(originalEvent);
        return ResponseEntity.ok(resource);
    }


}
