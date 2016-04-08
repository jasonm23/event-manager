package com.pinkpony.service;

import com.pinkpony.config.MarvinMediaTypes;
import com.pinkpony.model.CalendarEvent;
import com.pinkpony.model.CalendarEventMessageProjection;
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
import java.util.Date;
import java.util.Map;
import java.util.Optional;

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
        if(isMarvinRequest(request)){
            CalendarEventMessageProjection calendarEventMessageProjection = spelAwareProxyProjectionFactory.createProjection(CalendarEventMessageProjection.class, calendarEvent);
            calendarEventResource = new Resource<>(calendarEventMessageProjection);
        } else {
            //wrap our projection in a HateOS resource for response
            CalendarEventProjection calendarEventProjection = spelAwareProxyProjectionFactory.createProjection(CalendarEventProjection.class, calendarEvent);
            calendarEventResource = new Resource<>(calendarEventProjection);
        }

        return ControllerUtils.toResponseEntity(HttpStatus.CREATED, new HttpHeaders(), calendarEventResource);
    }

    private boolean isMarvinRequest(HttpServletRequest request) {
        return request.getHeader("Accept").equals(MarvinMediaTypes.MARVIN_JSON_MEDIATYPE_VALUE);
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

        String eventOwner = calendarEventMap.get("username");

        //We don't merge the "username" field on PATCH, because we don't allow changes to it
        calendarEventMap.remove("username");

        //Optional<CalendarEvent> optionalCalendarEvent = mergeCalendarEvent(calendarEventId, calendarEventMap);
        GenericMerge<CalendarEvent> genericMerge = new GenericMerge<>(calendarEventRepository);
        Optional<CalendarEvent> optionalCalendarEvent = genericMerge.mergeObject(calendarEventId,calendarEventMap);

        //break out if we can't find the calendar Event
        if(! optionalCalendarEvent.isPresent()) {
            return ControllerUtils.toResponseEntity(HttpStatus.BAD_REQUEST, new HttpHeaders(), null );
        }
        CalendarEvent updatedEvent = optionalCalendarEvent.get();


        //if validation fails we exit early
        Optional<ResponseEntity<ResourceSupport>> validationResult = ensureValidity(updatedEvent);
        if (validationResult.isPresent()){
            return validationResult.get();
        }

        //check if event is being updated after it has already started
        Date timeNow = new DateTime().toDate();
        if ( timeNow.compareTo(updatedEvent.getCalendarEventDateTime()) > 0 ) {
            return validateConstraint("calendarEventDateTime", "calendarEvent.calendarEventDateTime.field.inPast",calendarEventMap);
        }

        //check if event is being updated by someone other than event owner
        if (eventOwner != null && ! updatedEvent.getUsername().equals(eventOwner)){
            return validateConstraint("username", "calendarEvent.username.field.mismatch", calendarEventMap);
        }


        Resource<?> updatedResource = new Resource<>(updatedEvent);
        return ControllerUtils.toResponseEntity(HttpStatus.OK, new HttpHeaders(), updatedResource);

    }

    private ResponseEntity<ResourceSupport> validateConstraint(String fieldName, String i18nMessage, Map<String, String> calendarEventMap) {
        BindingResult binder = new MapBindingResult(calendarEventMap, "CalendarEvent");
        binder.rejectValue(fieldName, i18nMessage);

        RepositoryConstraintViolationExceptionMessage message = new RepositoryConstraintViolationExceptionMessage(new RepositoryConstraintViolationException(binder), new MessageSourceAccessor(messageSource));
        Resource<?> resource = new Resource<>(message);
        return ControllerUtils.toResponseEntity(HttpStatus.BAD_REQUEST, new HttpHeaders(), resource);
    }

    public ResponseEntity<ResourceSupport> showEvent(Long calendarEventId, HttpServletRequest request) {
        CalendarEvent calendarEvent = calendarEventRepository.findOne(calendarEventId);

        if (null == calendarEvent)
            return ControllerUtils.toResponseEntity(HttpStatus.BAD_REQUEST, new HttpHeaders(), null);

        Resource<?> calendarEventResource;
        if(isMarvinRequest(request)){
            CalendarEventMessageProjection calendarEventMessageProjection = spelAwareProxyProjectionFactory.createProjection(CalendarEventMessageProjection.class, calendarEvent);
            calendarEventResource = new Resource<>(calendarEventMessageProjection);
        } else {
            //wrap our projection in a HateOS resource for response
            CalendarEventProjection calendarEventProjection = spelAwareProxyProjectionFactory.createProjection(CalendarEventProjection.class, calendarEvent);
            calendarEventResource = new Resource<>(calendarEventProjection);
        }
        return ControllerUtils.toResponseEntity(HttpStatus.OK, new HttpHeaders(), calendarEventResource);
    }
}
