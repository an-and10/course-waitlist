package com.company.app.coursewaitlist.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.company.app.coursewaitlist.dto.DropResponse;
import com.company.app.coursewaitlist.dto.EnrollRequest;
import com.company.app.coursewaitlist.dto.EnrollResponse;
import com.company.app.coursewaitlist.model.DropOutcome;
import com.company.app.coursewaitlist.model.DropResult;
import com.company.app.coursewaitlist.model.EnrollOutcome;
import com.company.app.coursewaitlist.model.EnrollResult;
import com.company.app.coursewaitlist.model.OfferingState;
import com.company.app.coursewaitlist.service.WaitlistService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/offerings/{offeringId}")
public class EnrollmentController {

	private final WaitlistService waitlistService;

	public EnrollmentController(WaitlistService waitlistService) {
		this.waitlistService = waitlistService;
	}

	@PostMapping("/enrollments")
	public ResponseEntity<EnrollResponse> enroll(@PathVariable String offeringId,
			@Valid @RequestBody EnrollRequest request) {
		EnrollResult result = waitlistService.enroll(offeringId, request.learnerId());
		return ResponseEntity.status(statusFor(result.outcome())).body(EnrollResponse.from(result));
	}

	@DeleteMapping("/enrollments/{learnerId}")
	public ResponseEntity<DropResponse> drop(@PathVariable String offeringId, @PathVariable String learnerId) {
		DropResult result = waitlistService.drop(offeringId, learnerId);
		return ResponseEntity.status(statusFor(result.outcome())).body(DropResponse.from(result));
	}

	@GetMapping
	public OfferingState getOffering(@PathVariable String offeringId) {
		return waitlistService.getState(offeringId);
	}

	private HttpStatus statusFor(EnrollOutcome outcome) {
		return switch (outcome) {
			case ENROLLED, WAITLISTED -> HttpStatus.CREATED;
			case ALREADY_ENROLLED, ALREADY_WAITLISTED -> HttpStatus.OK;
			case PREREQUISITE_NOT_MET -> HttpStatus.UNPROCESSABLE_CONTENT;
		};
	}

	private HttpStatus statusFor(DropOutcome outcome) {
		return switch (outcome) {
			case DROPPED, LEFT_WAITLIST -> HttpStatus.OK;
			case NOT_ENROLLED -> HttpStatus.CONFLICT;
		};
	}
}
