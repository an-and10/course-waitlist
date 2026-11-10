package com.company.app.coursewaitlist.model;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * One course offering with its enrolled learners and FIFO waitlist.
 *
 * This class is not thread-safe by itself. {@code WaitlistService} holds the
 * offering's lock around every read and write, because a single command spans
 * both collections.
 */
public class CourseOffering {

	private final String id;
	private final int capacity;
	private final String prerequisiteCourseId;
	private final Set<String> enrolled = new LinkedHashSet<>();
	private final Set<String> waitlist = new LinkedHashSet<>(); // insertion order = FIFO

	public CourseOffering(String id, int capacity, String prerequisiteCourseId) {
		if (id == null || id.isBlank()) {
			throw new IllegalArgumentException("Offering id must not be blank");
		}
		if (capacity < 1) {
			throw new IllegalArgumentException("Capacity must be at least 1");
		}
		this.id = id;
		this.capacity = capacity;
		this.prerequisiteCourseId = prerequisiteCourseId;
	}

	public String getId() {
		return id;
	}

	public int getCapacity() {
		return capacity;
	}

	public String getPrerequisiteCourseId() {
		return prerequisiteCourseId;
	}

	public boolean isEnrolled(String learnerId) {
		return enrolled.contains(learnerId);
	}

	public boolean isWaitlisted(String learnerId) {
		return waitlist.contains(learnerId);
	}

	public boolean hasFreeSeat() {
		return seatsAvailable() > 0;
	}

	public int seatsAvailable() {
		return capacity - enrolled.size();
	}

	/**
	 * Callers check first; a violation here is a programming error, not a business
	 * outcome.
	 */
	public void enroll(String learnerId) {
		if (!hasFreeSeat()) {
			throw new IllegalStateException("Offering is full: " + id);
		}
		if (isWaitlisted(learnerId)) {
			throw new IllegalStateException("Learner is on the waitlist: " + learnerId);
		}
		enrolled.add(learnerId);
	}

	public void removeEnrolled(String learnerId) {
		enrolled.remove(learnerId);
	}

	/**
	 * Callers check first; a violation here is a programming error, not a business
	 * outcome.
	 */
	public void addToWaitlist(String learnerId) {
		if (isEnrolled(learnerId)) {
			throw new IllegalStateException("Learner is already enrolled: " + learnerId);
		}
		waitlist.add(learnerId);
	}

	public void removeFromWaitlist(String learnerId) {
		waitlist.remove(learnerId);
	}

	/** Removes and returns the learner who has waited longest, if any. */
	public Optional<String> pollWaitlistHead() {
		Iterator<String> iterator = waitlist.iterator();
		if (!iterator.hasNext()) {
			return Optional.empty();
		}
		String head = iterator.next();
		iterator.remove();
		return Optional.of(head);
	}

	/** Returns the learner's 1-based waitlist position. */
	public int waitlistPosition(String learnerId) {
		int position = 1;
		for (String waitingLearnerId : waitlist) {
			if (waitingLearnerId.equals(learnerId)) {
				return position;
			}
			position++;
		}
		throw new IllegalArgumentException("Learner is not waitlisted: " + learnerId);
	}

	public List<String> enrolledSnapshot() {
		return List.copyOf(enrolled);
	}

	public List<String> waitlistSnapshot() {
		return List.copyOf(waitlist);
	}
}
