package com.pinkpony.controller;

import com.pinkpony.model.Event;
import com.pinkpony.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RepositoryRestController
public class EventRestController {

   @Autowired
   private EventRepository eventRepository;

    @RequestMapping(value="/events/{eventId}", method = RequestMethod.PATCH)
    public @ResponseBody ResponseEntity<?> cancelEvent(@PathVariable Long eventId,
                                                      @RequestBody Map<String, String> eventMap) {Event originalEvent = eventRepository.findOne(eventId);
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
