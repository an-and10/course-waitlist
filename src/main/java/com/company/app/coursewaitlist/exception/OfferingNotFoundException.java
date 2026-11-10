package com.company.app.coursewaitlist.exception;

public class OfferingNotFoundException extends RuntimeException {

	public OfferingNotFoundException(String offeringId) {
		super("Offering not found: " + offeringId);
	}
}
