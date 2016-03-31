package com.pinkpony.controller;

import com.pinkpony.config.MarvinMediaTypes;
import com.pinkpony.model.CalendarEvent;
import com.pinkpony.repository.CalendarEventRepository;
import com.pinkpony.service.CalendarEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class CalendarEventRestController {

    @Autowired
    CalendarEventService calendarEventService;

    @Autowired
    MessageCodesResolver messageCodesResolver;

    // @InitBinder
    // protected void initBinder(WebDataBinder binder) {
    //     binder.setMessageCodesResolver(messageCodesResolver);
    //     binder.setValidator(new CalendarEventValidator());
    // }

    @Autowired
    CalendarEventRepository calendarEventRepository;

    @RequestMapping(value="/calendarEvents", method = RequestMethod.POST, consumes = {MediaType.ALL_VALUE, MediaType.APPLICATION_JSON_VALUE, MarvinMediaTypes.MARVIN_JSON_MEDIATYPE_VALUE})
    public @ResponseBody ResponseEntity<ResourceSupport> createEvent(@RequestBody CalendarEvent calendarEvent, HttpServletRequest request) {
        return calendarEventService.createEvent(calendarEvent, request);
    }

    // TODO / FIXME : Make this a CalendarEvent REST action, ie: POST calendarEvents/:id/cancellation
    @RequestMapping(value="/cancelledEvents/{calendarEventId}", method = RequestMethod.PATCH)
    public @ResponseBody ResponseEntity<?> updateEvent(@PathVariable Long calendarEventId,
                                                  @RequestBody Map<String, String> calendarEventMap) {
        return calendarEventService.cancelEvent(calendarEventId, calendarEventMap);
    }
}
