package com.pinkpony.controller;

import com.pinkpony.model.Event;
import com.pinkpony.repository.EventRepository;
import com.pinkpony.service.EventService;
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
    EventService eventService;

    @RequestMapping(value="/events/{eventId}", method = RequestMethod.PATCH)
    public @ResponseBody ResponseEntity<?> cancelEvent(@PathVariable Long eventId,
                                                      @RequestBody Map<String, String> eventMap) {
        return eventService.handleEventPUT(eventId, eventMap);
   }

}
