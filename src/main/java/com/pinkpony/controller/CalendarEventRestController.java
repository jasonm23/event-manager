package com.pinkpony.controller;

import com.pinkpony.service.CalendarEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RepositoryRestController
public class CalendarEventRestController {

    @Autowired
    CalendarEventService calendarEventService;

    @RequestMapping(value="/calendarEvents/{calendarEventId}", method = RequestMethod.PATCH)
    public @ResponseBody ResponseEntity<?> updateCalendarEvent(@PathVariable Long calendarEventId,
                                                               @RequestBody Map<String, String> calendarEventMap) {
        return calendarEventService.update(calendarEventId, calendarEventMap);
   }
}
