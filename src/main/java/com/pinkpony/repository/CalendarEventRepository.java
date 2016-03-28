package com.pinkpony.repository;

import com.pinkpony.model.CalendarEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(excerptProjection = CalendarEventProjection.class)
public interface CalendarEventRepository extends PagingAndSortingRepository<CalendarEvent, Long> {
}
