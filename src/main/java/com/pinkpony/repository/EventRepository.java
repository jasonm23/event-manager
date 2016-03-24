package com.pinkpony.repository;

import com.pinkpony.model.Event;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(excerptProjection = EventProjection.class)
public interface EventRepository extends PagingAndSortingRepository<Event, Long> {
}
