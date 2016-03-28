package com.pinkpony.controller;

import com.pinkpony.model.CalendarEvent;
import com.pinkpony.repository.CalendarEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RepositoryRestController
public class CalendarEventRestController {

   @Autowired
   private CalendarEventRepository calendarEventRepository;

    @RequestMapping(value="/calendarEvents/{calendarEventId}", method = RequestMethod.PATCH)
    public @ResponseBody ResponseEntity<?> cancelCalendarEvent(@PathVariable Long calendarEventId,
                                                      @RequestBody Map<String, String> calendarEventMap) {CalendarEvent originalCalendarEvent = calendarEventRepository.findOne(calendarEventId);
        Resource<CalendarEvent> resource = new Resource<CalendarEvent>(originalCalendarEvent);

        if (! originalCalendarEvent.getOrganizer().equals(calendarEventMap.get("organizer"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resource);
        }

        String cancellationStatus = calendarEventMap.get("cancelled").toLowerCase();
        if (! (cancellationStatus.equals("false") || cancellationStatus.equals("true"))){
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resource);
        }

        originalCalendarEvent.setCancelled(Boolean.parseBoolean(calendarEventMap.get("cancelled")));
        calendarEventRepository.save(originalCalendarEvent);
        return ResponseEntity.ok(resource);
   }
}
