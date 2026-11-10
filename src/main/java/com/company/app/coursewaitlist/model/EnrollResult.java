package com.company.app.coursewaitlist.model;

public record EnrollResult(EnrollOutcome outcome, String learnerId, Integer waitlistPosition) {
}
