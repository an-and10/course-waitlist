package com.company.app.coursewaitlist.model;

import java.util.List;

/** {@code skippedLearnerIds} is never null; it is empty when nobody was skipped. */
public record DropResult(DropOutcome outcome, String learnerId, String promotedLearnerId,
		List<String> skippedLearnerIds, int seatsAvailable) {
}
