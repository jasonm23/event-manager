package com.pinkpony.repository;

import com.pinkpony.model.CalendarEvent;
import com.pinkpony.model.CalendarEventProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Date;
import java.util.List;

@RepositoryRestResource
public interface CalendarEventRepository extends PagingAndSortingRepository<CalendarEvent, Long> {
    @RestResource(path = "upcomingEvents")
    @Query(value = "SELECT * FROM calendar_event WHERE calendar_event_date_time >= NOW() AND cancelled = 'f' ORDER BY calendar_event_date_time ASC;", nativeQuery = true)
    List<CalendarEvent> findUpcomingEvent();
}
