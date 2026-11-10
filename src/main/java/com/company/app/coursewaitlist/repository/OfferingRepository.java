package com.company.app.coursewaitlist.repository;

import java.util.Optional;

import com.company.app.coursewaitlist.model.CourseOffering;

public interface OfferingRepository {

	Optional<CourseOffering> findById(String id);

	void save(CourseOffering offering);
}
