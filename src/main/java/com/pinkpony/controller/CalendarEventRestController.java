package com.pinkpony.controller;

import com.pinkpony.config.AppConfig;
import com.pinkpony.model.CalendarEvent;
import com.pinkpony.repository.CalendarEventRepository;
import com.pinkpony.service.CalendarEventService;
import com.pinkpony.validator.CalendarEventValidator;
import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;
import com.sun.glass.ui.delegate.MenuItemDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.MessageSource;
import org.springframework.data.rest.core.event.BeforeCreateEvent;
import org.springframework.data.rest.webmvc.*;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.*;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

@RepositoryRestController
public class CalendarEventRestController {

    @Autowired
    MessageCodesResolver messageCodesResolver;

    // @InitBinder
    // protected void initBinder(WebDataBinder binder) {
    //     binder.setMessageCodesResolver(messageCodesResolver);
    //     binder.setValidator(new CalendarEventValidator());
    // }

    @Autowired
    CalendarEventService calendarEventService;

    @Autowired
    CalendarEventRepository calendarEventRepository;

    @RequestMapping(value="/calendarEvents/{calendarEventId}", method = RequestMethod.PATCH)
    public @ResponseBody ResponseEntity<?> updateCalendarEvent(@PathVariable Long calendarEventId,
                                                                      @RequestBody Map<String, String> calendarEventMap) {
        return calendarEventService.cancelEvent(calendarEventId, calendarEventMap);
    }

    @RequestMapping(value="/calendarEvents", method = RequestMethod.POST, consumes = {MediaType.ALL_VALUE, MediaType.APPLICATION_JSON_VALUE, AppConfig.MARVIN_JSON_MEDIATYPE_VALUE})
    public @ResponseBody ResponseEntity<ResourceSupport> createEvent(@RequestBody CalendarEvent calendarEvent, HttpServletRequest request) {
        return calendarEventService.createEvent(calendarEvent, request);
    }

}
