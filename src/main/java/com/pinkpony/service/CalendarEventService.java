package com.pinkpony.service;

import com.pinkpony.model.CalendarEvent;
import com.pinkpony.model.CalendarEventProjection;
import com.pinkpony.repository.CalendarEventRepository;
import com.pinkpony.util.GenericMerge;
import com.pinkpony.validator.CalendarEventValidator;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.data.rest.webmvc.ControllerUtils;
import org.springframework.data.rest.webmvc.support.RepositoryConstraintViolationExceptionMessage;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class CalendarEventService {

    @Autowired
    MessageSource messageSource;

    @Autowired
    SpelAwareProxyProjectionFactory spelAwareProxyProjectionFactory;

    @Autowired
    CalendarEventRepository calendarEventRepository;

    public Boolean cancelEvent(CalendarEvent event, String username) {
        return cancelEvent(event.getId(), username);
    }

    public Boolean cancelEvent(Long eventId, String username) {
        CalendarEvent event = calendarEventRepository.findOne(eventId);

        if (null == event || ! event.hasUsername(username)) {
            return false;
        }
        else {
            event.cancel();
            calendarEventRepository.save(event);
            return true;
        }
    }


    public  ResponseEntity<ResourceSupport> createEvent(CalendarEvent calendarEvent, HttpServletRequest request) {
        Optional<ResponseEntity<ResourceSupport>> optionalErrorResource = ensureValidity(calendarEvent);

        if (optionalErrorResource.isPresent()) return optionalErrorResource.get();

        CalendarEvent savedEvent = calendarEventRepository.save(calendarEvent);

        Resource<?> calendarEventResource;

        //wrap our projection in a HateOS resource for response
        CalendarEventProjection calendarEventProjection = spelAwareProxyProjectionFactory.createProjection(CalendarEventProjection.class, calendarEvent);
        calendarEventResource = new Resource<>(calendarEventProjection);

        return ControllerUtils.toResponseEntity(HttpStatus.CREATED, new HttpHeaders(), calendarEventResource);
    }

    public Optional<ResponseEntity<ResourceSupport>> ensureValidity(CalendarEvent calendarEvent) {
        CalendarEventValidator validator = new CalendarEventValidator();
        BindingResult result = new BeanPropertyBindingResult(calendarEvent, "CalendarEvent");
        validator.validate(calendarEvent, result);

        if (result.hasErrors()){
            RepositoryConstraintViolationExceptionMessage message = new RepositoryConstraintViolationExceptionMessage(new RepositoryConstraintViolationException(result), new MessageSourceAccessor(messageSource));
            Resource<?> errorResource = new Resource<>(message);
            return Optional.of(ControllerUtils.toResponseEntity(HttpStatus.BAD_REQUEST, new HttpHeaders(), errorResource));
        }
        return Optional.empty();
    }

    public ResponseEntity<ResourceSupport> patchEvent(Long calendarEventId, Map<String, String> calendarEventMap) {
        // Mutate calendarEventMap: remove the meta data we don't use
        removeMetadata(calendarEventMap);

        String eventOwner = calendarEventMap.get("username");


        //TODO: Refactor this so we only do one DB query for findOne() in this workflow.
        CalendarEvent currentEvent = calendarEventRepository.findOne(calendarEventId);
        Date savedDate = currentEvent.getCalendarEventDateTime();

        //We don't merge the "username" field on PATCH, because we don't allow changes to it
        calendarEventMap.remove("username");

        // if there is a calendarEventDateTime, move it to the calendarEventDateTimeString key
        if (null != calendarEventMap.get("calendarEventDateTime") ) {
            calendarEventMap.put("calendarEventDateTimeString", calendarEventMap.get("calendarEventDateTime"));
            calendarEventMap.remove("calendarEventDateTime");
        }

        //Optional<CalendarEvent> optionalCalendarEvent = mergeCalendarEvent(calendarEventId, calendarEventMap);
        GenericMerge<CalendarEvent> genericMerge = new GenericMerge<>(calendarEventRepository);
        Optional<CalendarEvent> optionalCalendarEvent = genericMerge.mergeObject(calendarEventId,calendarEventMap);

        //break out if we can't find the calendar Event
        if(! optionalCalendarEvent.isPresent()) {
            return ControllerUtils.toResponseEntity(HttpStatus.BAD_REQUEST, new HttpHeaders(), null );
        }
        CalendarEvent updatedCalendarEvent = optionalCalendarEvent.get();


        //if validation fails we exit early
        Optional<ResponseEntity<ResourceSupport>> validationResult = ensureValidity(updatedCalendarEvent);
        if (validationResult.isPresent()){
            return validationResult.get();
        }

        //check if event is being updated after it has already started
        // event is in the future, but we want to patch to update to the past
        Date timeNow = new DateTime().toDate();

        //check whether we are actually updating the Event Date, and IF SO, check that such update is not occuring in the past
        if ( null != calendarEventMap.get("calendarEventDateTimeString") && timeNow.compareTo(updatedCalendarEvent.getCalendarEventDateTime()) > 0 ) {
            return validateConstraint("calendarEventDateTime", "calendarEvent.calendarEventDateTime.field.cantSetDateInPast",calendarEventMap);
        }

        // event is in the past, we don't allow updates
        //We are just checking whether non-date-related-updates are actually allowed. I.e. check if event has expired.
        if ( timeNow.compareTo(savedDate) > 0 ) {
            return validateConstraint("calendarEventDateTime", "calendarEvent.calendarEventDateTime.field.eventHasAlreadyStarted",calendarEventMap);
        }

        //check if event is being updated by someone other than event owner
        if (eventOwner != null && ! updatedCalendarEvent.getUsername().equals(eventOwner)){
            return validateConstraint("username", "calendarEvent.username.field.mismatch", calendarEventMap);
        }

        calendarEventRepository.save(updatedCalendarEvent);
        Resource<?> updatedResource = new Resource<>(updatedCalendarEvent);
        return ControllerUtils.toResponseEntity(HttpStatus.OK, new HttpHeaders(), updatedResource);

    }

    private void removeMetadata(Map<String, String> calendarEventMap) {
        calendarEventMap.remove("received_at");
        calendarEventMap.remove("channel");
        calendarEventMap.remove("command");
    }

    private ResponseEntity<ResourceSupport> validateConstraint(String fieldName, String i18nMessage, Map<String, String> calendarEventMap) {
        BindingResult binder = new MapBindingResult(calendarEventMap, "CalendarEvent");
        binder.rejectValue(fieldName, i18nMessage);

        RepositoryConstraintViolationExceptionMessage message = new RepositoryConstraintViolationExceptionMessage(new RepositoryConstraintViolationException(binder), new MessageSourceAccessor(messageSource));
        Resource<?> resource = new Resource<>(message);
        return ControllerUtils.toResponseEntity(HttpStatus.BAD_REQUEST, new HttpHeaders(), resource);
    }

    public ResponseEntity<ResourceSupport> showUpcomingEventMessage() {
        List<CalendarEvent> upcomingEvents = calendarEventRepository.findUpcomingEvent();
        StringBuffer buffer = new StringBuffer("");

        for( CalendarEvent event : upcomingEvents) {
            buffer.append(event.showMessage());
        }

        Resource<?> resource = new Resource<>(buffer);
        return ControllerUtils.toResponseEntity(HttpStatus.OK, new HttpHeaders(), resource);

    }
}
