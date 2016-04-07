package com.pinkpony.controller;

import com.pinkpony.config.MarvinMediaTypes;
import com.pinkpony.model.CalendarEvent;
import com.pinkpony.repository.CalendarEventRepository;
import com.pinkpony.service.CalendarEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ControllerUtils;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RepositoryRestController
public class CalendarEventRepositoryRestController {

    @Autowired
    CalendarEventService calendarEventService;

    @Autowired
    MessageCodesResolver messageCodesResolver;

    @Autowired
    CalendarEventRepository calendarEventRepository;

    @RequestMapping(value="/calendarEvents", method = RequestMethod.POST, consumes = {MediaType.ALL_VALUE, MediaType.APPLICATION_JSON_VALUE, MarvinMediaTypes.MARVIN_JSON_MEDIATYPE_VALUE})
    public @ResponseBody ResponseEntity<ResourceSupport> createEvent( @RequestBody CalendarEvent calendarEvent, HttpServletRequest request) {
        return calendarEventService.createEvent(calendarEvent, request);
    }

    @RequestMapping(value="/calendarEvents/{calendarEventId}", method = RequestMethod.PATCH)
    public @ResponseBody ResponseEntity<ResourceSupport> updateEvent(@PathVariable Long calendarEventId,
                                                                     @RequestBody Map<String, String> calendarEventMap) {
        return calendarEventService.patchEvent(calendarEventId, calendarEventMap);
    }

    @RequestMapping(value="/calendarEvents/{calendarEventId}", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<ResourceSupport> showEvent(@PathVariable Long calendarEventId, HttpServletRequest request) {
        return calendarEventService.showEvent(calendarEventId, request);
    }

    @RequestMapping(value="/calendarEvents/{calendarEventId}", method = RequestMethod.PUT)
    public @ResponseBody ResponseEntity<ResourceSupport> updateEvent(@PathVariable Long calendarEventId) {
        return ControllerUtils.toResponseEntity(HttpStatus.METHOD_NOT_ALLOWED, new HttpHeaders(), null);
    }
}
