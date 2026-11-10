package com.company.app.coursewaitlist.dto;

import java.util.List;

import com.company.app.coursewaitlist.model.DropResult;

public record DropResponse(String outcome, String learnerId, String promoted, List<String> skipped,
		int seatsAvailable) {

	public static DropResponse from(DropResult result) {
		return new DropResponse(result.outcome().name(), result.learnerId(), result.promotedLearnerId(),
				result.skippedLearnerIds(), result.seatsAvailable());
	}
}
