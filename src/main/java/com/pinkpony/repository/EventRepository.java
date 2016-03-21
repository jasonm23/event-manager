package com.pinkpony.repository;

import com.pinkpony.model.Event;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface EventRepository extends PagingAndSortingRepository<Event, Long>{
}
