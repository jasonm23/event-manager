package com.pinkpony.service;

import com.pinkpony.config.AppConfig;
import com.pinkpony.model.CalendarEvent;
import com.pinkpony.model.CalendarEventMessageProjection;
import com.pinkpony.model.CalendarEventProjection;
import com.pinkpony.repository.CalendarEventRepository;
import com.pinkpony.validator.CalendarEventValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.data.rest.core.ValidationErrors;
import org.springframework.data.rest.webmvc.ControllerUtils;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.support.RepositoryConstraintViolationExceptionMessage;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CalendarEventService {

    @Autowired
    SpelAwareProxyProjectionFactory spelAwareProxyProjectionFactory;

    @Autowired
    CalendarEventRepository calendarEventRepository;

    public ResponseEntity<?> cancelEvent(Long eventId, Map<String, String> calendarEventMap) {
        CalendarEvent originalCalendarEvent = calendarEventRepository.findOne(eventId);

        if( originalCalendarEvent == null ) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }

        Resource<CalendarEvent> resource = new Resource<CalendarEvent>(originalCalendarEvent);

        if (calendarEventMap.get("username") == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resource);
        }

        if (! originalCalendarEvent.getUsername().equals(calendarEventMap.get("username"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resource);
        }

        if(! originalCalendarEvent.isCancelled()) {
            originalCalendarEvent.setCancelled(true);
            calendarEventRepository.save(originalCalendarEvent);
        }

        return ResponseEntity.ok(resource);
    }

    public  ResponseEntity<ResourceSupport> createEvent(CalendarEvent calendarEvent, HttpServletRequest request) {

        //Perform validation first
        CalendarEventValidator validator = new CalendarEventValidator();
        BindingResult result = new BeanPropertyBindingResult(calendarEvent, "CalendarEvent");
        validator.validate(calendarEvent, result);

        if (result.hasErrors()){
            RepositoryConstraintViolationExceptionMessage message = new RepositoryConstraintViolationExceptionMessage(new RepositoryConstraintViolationException(result), new MessageSourceAccessor(messageSource));
            Resource<?> errorResource = new Resource<>(message);
            return ControllerUtils.toResponseEntity(HttpStatus.CREATED, new HttpHeaders(), errorResource);
        }

        //persist our data
        CalendarEvent savedEvent = calendarEventRepository.save(calendarEvent);

        //get the request accept header. inspect
        Resource<?> calendarEventResource;
        if(request.getHeader("Accept").equals(AppConfig.MARVIN_JSON_MEDIATYPE_VALUE)){
            CalendarEventMessageProjection calendarEventMessageProjection = spelAwareProxyProjectionFactory.createProjection(CalendarEventMessageProjection.class, calendarEvent);
            calendarEventResource = new Resource<>(calendarEventMessageProjection);
        } else {
            //wrap our projection in a HateOS resource for response
            CalendarEventProjection calendarEventProjection = spelAwareProxyProjectionFactory.createProjection(CalendarEventProjection.class, calendarEvent);
            calendarEventResource = new Resource<>(calendarEventProjection);
        }

        return ControllerUtils.toResponseEntity(HttpStatus.CREATED, new HttpHeaders(), calendarEventResource);

    }
}
