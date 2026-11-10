package com.company.app.coursewaitlist.model;

import java.util.List;

public record OfferingState(String offeringId, int capacity, String prerequisiteCourseId,
		int seatsAvailable, List<String> enrolled, List<String> waitlist) {
}
