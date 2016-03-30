package com.pinkpony.controller;

import com.pinkpony.service.CalendarEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class CalendarEventRestController {

    @Autowired
    CalendarEventService calendarEventService;

    @RequestMapping(value="/cancelledEvents/{calendarEventId}", method = RequestMethod.PATCH)
    public @ResponseBody ResponseEntity<?> updateCalendarEvent(@PathVariable Long calendarEventId,
                                                                      @RequestBody Map<String, String> calendarEventMap) {
        return calendarEventService.cancelEvent(calendarEventId, calendarEventMap);
   }
}
