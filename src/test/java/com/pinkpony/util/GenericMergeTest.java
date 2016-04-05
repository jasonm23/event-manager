package com.pinkpony.util;

import com.pinkpony.model.CalendarEvent;
import com.pinkpony.repository.CalendarEventRepository;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GenericMergeTest{

    @Mock
    CalendarEventRepository calendarEventRepository;

    GenericMerge<CalendarEvent> genericMerge;

    Map<String, String> calendarEventMap;
    CalendarEvent calendarEvent;
    Date date;

    @Before
    public void setUp() throws Exception {
        calendarEventMap = new HashMap<>();
        calendarEventMap.put("name", "new name");

        date = new DateTime().toDate();

        calendarEvent = new CalendarEvent();
        calendarEvent.setName("Spring Boot Night");
        calendarEvent.setDescription("Wanna learn how to boot?");
        calendarEvent.setVenue("Arrowhead Lounge");
        calendarEvent.setCalendarEventDateTime(date);
        calendarEvent.setCalendarEventDateTimeString(CalendarEvent.dateFormat.format(date));
        calendarEvent.setUsername("Holly");

        when(calendarEventRepository.findOne(calendarEvent.getId())).thenReturn(calendarEvent);

        genericMerge = new GenericMerge<>(calendarEventRepository);
    }

    @Test
    public void testMergeUpdateParamAndLeaveOthersUnchanged() {
        Optional<CalendarEvent> optionalCalendarEvent = genericMerge.mergeObject(calendarEvent.getId(), calendarEventMap);
        CalendarEvent calendarEvent = optionalCalendarEvent.get();

        assertEquals("new name", calendarEvent.getName());
        assertEquals("Wanna learn how to boot?", calendarEvent.getDescription());
        assertEquals("Arrowhead Lounge", calendarEvent.getVenue());
        assertEquals(date, calendarEvent.getCalendarEventDateTime());
        assertEquals(CalendarEvent.dateFormat.format(date), calendarEvent.getCalendarEventDateTimeString());
        assertEquals("Holly", calendarEvent.getUsername());
    }

    @Test
    public void testMergingWrongDataLeavesBeanUnchanged(){

        calendarEventMap.put("Skadoinkadoink", "Paloinkidoink");

        Optional<CalendarEvent> optionalCalendarEvent = genericMerge.mergeObject(calendarEvent.getId(), calendarEventMap);

        //no partial failure. We dont' merge if even just one field fails to merge
        assertFalse(optionalCalendarEvent.isPresent());
    }
}