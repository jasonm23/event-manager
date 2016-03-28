package com.pinkpony.validator;

import com.pinkpony.model.Event;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;

import static org.hamcrest.Matchers.hasItemInArray;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class EventValidatorTest {
    EventValidator validator;
    Event event;

    @Before
    public void setUp() {
        event = makeEvent();
        validator = new EventValidator();
    }

    @Test
    public void acceptsValidEvent() {
        assertValid(event, validator);
    }

    @Test
    public void rejectsEmptyName() throws Exception {
        event.setName("");
        assertInvalid(event, validator, "name", "event.name.field.empty");
    }

    @Test
    public void rejectsNullName() throws Exception {
        event.setName(null);
        assertInvalid(event, validator, "name", "event.name.field.empty");
    }

    @Test
    public void rejectsEmptyDescription() throws Exception {
        event.setDescription("");
        assertInvalid(event, validator, "description", "event.description.field.empty");
    }

    @Test
    public void rejectsNullDescription() throws Exception {
        event.setDescription(null);
        assertInvalid(event, validator, "description", "event.description.field.empty");
    }

    @Test
    public void rejectsEmptyEventDateTime() throws Exception {
        event.setEventDateTimeString("");
        assertInvalid(event, validator, "eventDateTimeString", "event.eventDateTime.field.empty");
    }

    @Test
    public void rejectsNullEventDateTime() throws Exception {
        event.setEventDateTimeString(null);
        assertInvalid(event, validator, "eventDateTimeString", "event.eventDateTime.field.empty");
    }

    @Test
    public void rejectsEmptyVenue() {
        event.setVenue("");
        assertInvalid(event, validator, "venue", "event.venue.field.empty");
    }

    @Test
    public void rejectsNullVenue() {
        event.setVenue(null);
        assertInvalid(event, validator, "venue", "event.venue.field.empty");
    }

    @Test
    public void rejectsEmptyOrganizer() {
        event.setOrganizer("");
        assertInvalid(event, validator, "organizer", "event.organizer.field.empty");
    }

    @Test
    public void rejectsNullOrganizer() {
        event.setOrganizer(null);
        assertInvalid(event, validator, "organizer", "event.organizer.field.empty");
    }

    @Test
    public void rejectsEventDateTimeWithInvalidFormat() throws Exception {
        event.setEventDateTimeString("2015-03-11T11:00:00"); // missing time zone
        assertInvalid(event, validator, "eventDateTimeString", "event.eventDateTime.field.invalid");
    }

    private Event makeEvent() {
        Event newEvent = new Event();
        newEvent.setName("Spring Boot Night");
        newEvent.setDescription("Wanna learn how to boot?");
        newEvent.setVenue("Arrowhead Lounge");
        newEvent.setEventDateTimeString("2016-03-18T14:33:00+0000");
        newEvent.setOrganizer("Holly");

        return newEvent;
    }

    private void assertInvalid(Event event, Validator validator, String property, String errorStringKey) {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(event, "Event");
        validator.validate(event, errors);

        assertEquals(1, errors.getErrorCount());
        assertThat(errors.getFieldError(property).getCodes(), hasItemInArray(errorStringKey));
    }

    private void assertValid(Object object, Validator validator) {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(object, "_");
        validator.validate(object, errors);
        assertEquals(0, errors.getErrorCount());
    }
}