package com.pinkpony.service;

import com.pinkpony.config.MarvinMediaTypes;
import com.pinkpony.model.CalendarEvent;
import com.pinkpony.model.CalendarEventMessageProjection;
import com.pinkpony.model.CalendarEventProjection;
import com.pinkpony.repository.CalendarEventRepository;
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
import org.springframework.util.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

@Service
public class CalendarEventService {

    @Autowired
    MessageSource messageSource;

    @Autowired
    SpelAwareProxyProjectionFactory spelAwareProxyProjectionFactory;

    @Autowired
    CalendarEventRepository calendarEventRepository;

    public ResponseEntity<?> cancelEvent(Long eventId, Map<String, String> eventParams) {
        CalendarEvent event = calendarEventRepository.findOne(eventId);

        if( event == null ) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(""); }

        Resource<CalendarEvent> resource = new Resource<CalendarEvent>(event);

        if (missingUsername(eventParams)){ return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resource); }

        if (usernameMismatch(eventParams, event)) { return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resource); }

        if(! event.isCancelled()) {
            event.cancel();
            calendarEventRepository.save(event);
        }

        return ResponseEntity.ok(resource);
    }

    private boolean usernameMismatch(Map<String, String> calendarEventMap, CalendarEvent originalCalendarEvent) {
        return ! originalCalendarEvent.getUsername().equals(calendarEventMap.get("username"));
    }

    private boolean missingUsername(Map<String, String> calendarEventMap) {
        return calendarEventMap.get("username") == null;
    }

    public  ResponseEntity<ResourceSupport> createEvent(CalendarEvent calendarEvent, HttpServletRequest request) {
        ResponseEntity<ResourceSupport> errorResource = ensureValidity(calendarEvent);
        if (errorResource != null) return errorResource;

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

    private ResponseEntity<ResourceSupport> ensureValidity(CalendarEvent calendarEvent) {
        CalendarEventValidator validator = new CalendarEventValidator();
        BindingResult result = new BeanPropertyBindingResult(calendarEvent, "CalendarEvent");
        validator.validate(calendarEvent, result);

        if (result.hasErrors()){
            RepositoryConstraintViolationExceptionMessage message = new RepositoryConstraintViolationExceptionMessage(new RepositoryConstraintViolationException(result), new MessageSourceAccessor(messageSource));
            Resource<?> errorResource = new Resource<>(message);
            return ControllerUtils.toResponseEntity(HttpStatus.BAD_REQUEST, new HttpHeaders(), errorResource);
        }
        return null;
    }

    public ResponseEntity<ResourceSupport> patchEvent(Long calendarEventId, Map<String, String> calendarEventMap) {

        CalendarEvent originalCalendarEvent = mergeCalendarEvent(calendarEventId, calendarEventMap);

        //Perform validation first
        CalendarEventValidator validator = new CalendarEventValidator();
        BindingResult result = new BeanPropertyBindingResult(originalCalendarEvent, "CalendarEvent");
        validator.validate(originalCalendarEvent, result);

        if (result.hasErrors()){
            RepositoryConstraintViolationExceptionMessage message = new RepositoryConstraintViolationExceptionMessage(new RepositoryConstraintViolationException(result), new MessageSourceAccessor(messageSource));
            Resource<?> errorResource = new Resource<>(message);
            return ControllerUtils.toResponseEntity(HttpStatus.BAD_REQUEST, new HttpHeaders(), errorResource);
        }

        //check if event is being updated after it has already started

        if( originalCalendarEvent == null ) {
            return ControllerUtils.toResponseEntity(HttpStatus.BAD_REQUEST, new HttpHeaders(), null );
        }

        Date timeNow = new DateTime().toDate();
        if ( timeNow.compareTo(originalCalendarEvent.getCalendarEventDateTime()) > 0 ) {
            BindingResult binder = new MapBindingResult(calendarEventMap, "CalendarEvent");
            binder.rejectValue("calendarEventDateTime", "calendarEvent.calendarEventDateTime.field.inPast");

            RepositoryConstraintViolationExceptionMessage message = new RepositoryConstraintViolationExceptionMessage(new RepositoryConstraintViolationException(binder), new MessageSourceAccessor(messageSource));
            Resource<?> resource = new Resource<>(message);
            return ControllerUtils.toResponseEntity(HttpStatus.BAD_REQUEST, new HttpHeaders(), resource);
        }

        if (calendarEventMap.get("username") != null && ! originalCalendarEvent.getUsername().equals(calendarEventMap.get("username"))){

            calendarEventMap.put("username", originalCalendarEvent.getUsername());
            BindingResult binder = new MapBindingResult(calendarEventMap, "CalendarEvent");
            binder.rejectValue("username", "calendarEvent.username.field.mismatch");

            RepositoryConstraintViolationExceptionMessage message = new RepositoryConstraintViolationExceptionMessage(new RepositoryConstraintViolationException(binder), new MessageSourceAccessor(messageSource));
            Resource<?> resource = new Resource<>(message);
            return ControllerUtils.toResponseEntity(HttpStatus.BAD_REQUEST, new HttpHeaders(), resource);
        }

        Resource<?> originalResource = new Resource<>(originalCalendarEvent);
        return ControllerUtils.toResponseEntity(HttpStatus.OK, new HttpHeaders(), originalResource);

    }

    private CalendarEvent mergeCalendarEvent(Long calendarEventId, Map<String, String> calendarEventMap) {
        CalendarEvent originalCalendarEvent = calendarEventRepository.findOne(calendarEventId);

        for(String key: calendarEventMap.keySet()){
            try {
                if(! key.equals("username")) {
                    //generate setter method from key name
                    String methodName = "set" + StringUtils.capitalize(key);

                    String value = calendarEventMap.get(key);

                    Method method = originalCalendarEvent.getClass().getMethod(methodName, String.class);
                    //apply the setter method with the value of *this* key

                    method.invoke(originalCalendarEvent, value);
                    //originalCalendarEvent.applyMethod("setterMethod", value);

                }
            }catch(NoSuchMethodException nsme){
               nsme.printStackTrace();
            }catch(IllegalArgumentException iae){
               iae.printStackTrace();
            }catch(InvocationTargetException ite){
                ite.printStackTrace();
            }catch(IllegalAccessException iae){
                iae.printStackTrace();
            }
        }
        return originalCalendarEvent;
    }
}
