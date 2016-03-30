package com.pinkpony.repository;

import com.pinkpony.model.Rsvp;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RsvpRepository extends PagingAndSortingRepository<Rsvp, Long> {
}
