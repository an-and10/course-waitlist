package com.company.app.coursewaitlist.model;

import java.util.Set;

public record Learner(String id, Set<String> completedCourseIds) {

	public Learner {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("Learner id must not be blank");
		}
		completedCourseIds = completedCourseIds == null ? Set.of() : Set.copyOf(completedCourseIds);
	}

	public boolean hasCompleted(String courseId) {
		return completedCourseIds.contains(courseId);
	}
}
