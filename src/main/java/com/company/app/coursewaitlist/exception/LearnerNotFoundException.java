package com.company.app.coursewaitlist.exception;

public class LearnerNotFoundException extends RuntimeException {

	public LearnerNotFoundException(String learnerId) {
		super("Learner not found: " + learnerId);
	}
}
