package com.company.app.coursewaitlist.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.company.app.coursewaitlist.model.Learner;

@Repository
public class InMemoryLearnerRepository implements LearnerRepository {

	private final Map<String, Learner> learners = new ConcurrentHashMap<>();

	@Override
	public Optional<Learner> findById(String id) {
		return id == null ? Optional.empty() : Optional.ofNullable(learners.get(id));
	}

	@Override
	public void save(Learner learner) {
		learners.put(learner.id(), learner);
	}
}
