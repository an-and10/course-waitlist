package com.company.app.coursewaitlist.repository;

import java.util.Optional;

import com.company.app.coursewaitlist.model.Learner;

public interface LearnerRepository {

	Optional<Learner> findById(String id);

	void save(Learner learner);
}
