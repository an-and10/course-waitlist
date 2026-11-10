package com.company.app.coursewaitlist.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.company.app.coursewaitlist.model.CourseOffering;

@Repository
public class InMemoryOfferingRepository implements OfferingRepository {

	private final Map<String, CourseOffering> offerings = new ConcurrentHashMap<>();

	@Override
	public Optional<CourseOffering> findById(String id) {
		return id == null ? Optional.empty() : Optional.ofNullable(offerings.get(id));
	}

	@Override
	public void save(CourseOffering offering) {
		offerings.put(offering.getId(), offering);
	}
}
