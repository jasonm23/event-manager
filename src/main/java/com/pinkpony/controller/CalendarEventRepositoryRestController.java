package com.pinkpony.controller;

import com.pinkpony.model.CalendarEvent;
import com.pinkpony.repository.CalendarEventRepository;
import com.pinkpony.service.CalendarEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ControllerUtils;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

    private boolean usernameMismatch(CalendarEvent event, String username) {
        return ! event.getUsername().equals(username);
    }

    private boolean missingUsername(Map<String, String> calendarEventMap) {
        return calendarEventMap.get("username") == null;
    }

    @RequestMapping(value="/calendarEvents", method = RequestMethod.POST )
    public @ResponseBody ResponseEntity<ResourceSupport> createEvent( @RequestBody CalendarEvent calendarEvent, HttpServletRequest request) {
        return calendarEventService.createEvent(calendarEvent, request);
    }

    public ResponseEntity<ResourceSupport> statusResponse(HttpStatus status, Map<String, String> params) {
        Resource<Map<String, String>> defaultResourceMap = new Resource<>(params);
        return ResponseEntity.status(status).body(defaultResourceMap);
    }

    @RequestMapping(value="/calendarEvents/{calendarEventId}/cancel", method = RequestMethod.PATCH)
    public @ResponseBody ResponseEntity<ResourceSupport> cancelEvent(@PathVariable Long calendarEventId,
                                                                     @RequestBody Map<String, String> calendarEventMap) {
        String usernameParam = calendarEventMap.get("username");

        if (null == usernameParam){
            return statusResponse(HttpStatus.BAD_REQUEST, calendarEventMap);
        }

        CalendarEvent event = calendarEventRepository.findOne(calendarEventId);

        if (null == event) {
            return statusResponse(HttpStatus.NOT_FOUND, calendarEventMap);
        }

        if (calendarEventService.cancelEvent(event, usernameParam)) {
            Resource<CalendarEvent> resource = new Resource<>(event);
            return ResponseEntity.ok(resource);
        }
        else {
            return statusResponse(HttpStatus.FORBIDDEN, calendarEventMap);
        }
    }

    @RequestMapping(value="/calendarEvents/{calendarEventId}", method = RequestMethod.PATCH)
    public @ResponseBody ResponseEntity<ResourceSupport> updateEvent(@PathVariable Long calendarEventId,
                                                                     @RequestBody Map<String, String> calendarEventMap) {
        return calendarEventService.patchEvent(calendarEventId, calendarEventMap);
    }

    @RequestMapping(value="/calendarEvents/{calendarEventId}", method = RequestMethod.PUT)
    public @ResponseBody ResponseEntity<ResourceSupport> updateEvent(@PathVariable Long calendarEventId) {
        return ControllerUtils.toResponseEntity(HttpStatus.METHOD_NOT_ALLOWED, new HttpHeaders(), null);
    }
}
