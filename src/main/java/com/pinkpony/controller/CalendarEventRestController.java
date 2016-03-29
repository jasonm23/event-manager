package com.pinkpony.controller;

import com.pinkpony.config.AppConfig;
import com.pinkpony.model.CalendarEvent;
import com.pinkpony.service.CalendarEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class CalendarEventRestController {

    @Autowired
    CalendarEventService calendarEventService;

    @RequestMapping(value="/calendarEvents/{calendarEventId}", method = RequestMethod.PATCH)
    public @ResponseBody ResponseEntity<?> updateCalendarEvent(@PathVariable Long calendarEventId,
                                                                      @RequestBody Map<String, String> calendarEventMap) {
        return calendarEventService.cancelEvent(calendarEventId, calendarEventMap);
   }


    @RequestMapping(value="/calendarEvents", method = RequestMethod.POST, consumes = AppConfig.MARVIN_JSON_MEDIATYPE)
    public @ResponseBody ResponseEntity<?> createEvent(@RequestBody CalendarEvent calendarEvent, HttpRequest request) {
        return calendarEventService.createEvent(calendarEvent, request);
    }
}
