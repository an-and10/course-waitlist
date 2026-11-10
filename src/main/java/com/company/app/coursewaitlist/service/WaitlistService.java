package com.company.app.coursewaitlist.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import com.company.app.coursewaitlist.exception.LearnerNotFoundException;
import com.company.app.coursewaitlist.exception.OfferingNotFoundException;
import com.company.app.coursewaitlist.model.CourseOffering;
import com.company.app.coursewaitlist.model.DropOutcome;
import com.company.app.coursewaitlist.model.DropResult;
import com.company.app.coursewaitlist.model.EnrollOutcome;
import com.company.app.coursewaitlist.model.EnrollResult;
import com.company.app.coursewaitlist.model.Learner;
import com.company.app.coursewaitlist.model.OfferingState;
import com.company.app.coursewaitlist.repository.LearnerRepository;
import com.company.app.coursewaitlist.repository.OfferingRepository;

/**
 * Enroll, drop and waitlist promotion for a course offering.
 *
 * <p>Each command checks and then changes both the enrolled set and the waitlist,
 * so every public method holds the offering's lock for its whole duration.
 * Normal business outcomes are returned as results; only an unknown offering or
 * learner throws.
 */
@Service
public class WaitlistService {

	private final OfferingRepository offerings;
	private final LearnerRepository learners;
	private final EligibilityPolicy eligibilityPolicy;
	private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

	public WaitlistService(OfferingRepository offerings, LearnerRepository learners,
			EligibilityPolicy eligibilityPolicy) {
		this.offerings = offerings;
		this.learners = learners;
		this.eligibilityPolicy = eligibilityPolicy;
	}

	public EnrollResult enroll(String offeringId, String learnerId) {
		CourseOffering offering = findOffering(offeringId);

		ReentrantLock lock = lockFor(offeringId);
		lock.lock();
		try {
			// Read inside the lock so eligibility is judged on the record current at
			// decision time, not one read before waiting for the lock.
			Learner learner = findLearner(learnerId);

			// Duplicates come first: a repeat request never changes state, even if
			// the learner's record has changed since they joined.
			if (offering.isEnrolled(learnerId)) {
				return new EnrollResult(EnrollOutcome.ALREADY_ENROLLED, learnerId, null);
			}
			if (offering.isWaitlisted(learnerId)) {
				return new EnrollResult(EnrollOutcome.ALREADY_WAITLISTED, learnerId,
						offering.waitlistPosition(learnerId));
			}
			if (!eligibilityPolicy.isEligible(learner, offering)) {
				return new EnrollResult(EnrollOutcome.PREREQUISITE_NOT_MET, learnerId, null);
			}
			if (offering.hasFreeSeat()) {
				offering.enroll(learnerId);
				return new EnrollResult(EnrollOutcome.ENROLLED, learnerId, null);
			}
			offering.addToWaitlist(learnerId);
			return new EnrollResult(EnrollOutcome.WAITLISTED, learnerId, offering.waitlistPosition(learnerId));
		}
		finally {
			lock.unlock();
		}
	}

	public DropResult drop(String offeringId, String learnerId) {
		CourseOffering offering = findOffering(offeringId);
		findLearner(learnerId);

		ReentrantLock lock = lockFor(offeringId);
		lock.lock();
		try {
			if (offering.isWaitlisted(learnerId)) {
				offering.removeFromWaitlist(learnerId);
				return new DropResult(DropOutcome.LEFT_WAITLIST, learnerId, null, List.of(),
						offering.seatsAvailable());
			}
			if (!offering.isEnrolled(learnerId)) {
				return new DropResult(DropOutcome.NOT_ENROLLED, learnerId, null, List.of(),
						offering.seatsAvailable());
			}

			offering.removeEnrolled(learnerId);
			List<String> skipped = new ArrayList<>();
			String promoted = promoteNextEligible(offering, skipped);
			return new DropResult(DropOutcome.DROPPED, learnerId, promoted, skipped, offering.seatsAvailable());
		}
		finally {
			lock.unlock();
		}
	}

	public OfferingState getState(String offeringId) {
		CourseOffering offering = findOffering(offeringId);

		ReentrantLock lock = lockFor(offeringId);
		lock.lock();
		try {
			return new OfferingState(offering.getId(), offering.getCapacity(), offering.getPrerequisiteCourseId(),
					offering.seatsAvailable(), offering.enrolledSnapshot(), offering.waitlistSnapshot());
		}
		finally {
			lock.unlock();
		}
	}

	/**
	 * Fills the free seat from the front of the waitlist. Eligibility is checked
	 * again here because a learner's record can change while they wait. Anyone no
	 * longer eligible is removed from the waitlist and added to {@code skipped}.
	 * Must be called while holding the offering's lock.
	 *
	 * @return the promoted learner's id, or null if nobody could be promoted
	 */
	private String promoteNextEligible(CourseOffering offering, List<String> skipped) {
		String promoted = null;
		while (offering.hasFreeSeat()) {
			Optional<String> head = offering.pollWaitlistHead();
			if (head.isEmpty()) {
				break;
			}
			String candidateId = head.get();
			if (isStillEligible(candidateId, offering)) {
				offering.enroll(candidateId);
				promoted = candidateId;
			}
			else {
				skipped.add(candidateId);
			}
		}
		return promoted;
	}

	private boolean isStillEligible(String learnerId, CourseOffering offering) {
		return learners.findById(learnerId)
			.map(learner -> eligibilityPolicy.isEligible(learner, offering))
			.orElse(false);
	}

	private CourseOffering findOffering(String offeringId) {
		return offerings.findById(offeringId).orElseThrow(() -> new OfferingNotFoundException(offeringId));
	}

	private Learner findLearner(String learnerId) {
		return learners.findById(learnerId).orElseThrow(() -> new LearnerNotFoundException(learnerId));
	}

	private ReentrantLock lockFor(String offeringId) {
		return locks.computeIfAbsent(offeringId, key -> new ReentrantLock());
	}
}
