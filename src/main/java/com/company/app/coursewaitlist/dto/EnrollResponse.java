package com.company.app.coursewaitlist.dto;

import com.company.app.coursewaitlist.model.EnrollResult;

public record EnrollResponse(String outcome, String learnerId, Integer waitlistPosition) {

	public static EnrollResponse from(EnrollResult result) {
		return new EnrollResponse(result.outcome().name(), result.learnerId(), result.waitlistPosition());
	}
}
