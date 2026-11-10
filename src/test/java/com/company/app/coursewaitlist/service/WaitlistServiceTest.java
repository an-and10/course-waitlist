package com.company.app.coursewaitlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.company.app.coursewaitlist.exception.LearnerNotFoundException;
import com.company.app.coursewaitlist.exception.OfferingNotFoundException;
import com.company.app.coursewaitlist.model.CourseOffering;
import com.company.app.coursewaitlist.model.DropOutcome;
import com.company.app.coursewaitlist.model.DropResult;
import com.company.app.coursewaitlist.model.EnrollOutcome;
import com.company.app.coursewaitlist.model.EnrollResult;
import com.company.app.coursewaitlist.model.Learner;
import com.company.app.coursewaitlist.model.OfferingState;
import com.company.app.coursewaitlist.repository.InMemoryLearnerRepository;
import com.company.app.coursewaitlist.repository.InMemoryOfferingRepository;
import com.company.app.coursewaitlist.repository.LearnerRepository;
import com.company.app.coursewaitlist.repository.OfferingRepository;

/** One offering with capacity 2 and prerequisite CS101. */
class WaitlistServiceTest {

	private static final String OFFERING = "OFFERING-1";
	private static final String PREREQUISITE = "CS101";

	private LearnerRepository learners;
	private WaitlistService service;

	@BeforeEach
	void setUp() {
		OfferingRepository offerings = new InMemoryOfferingRepository();
		learners = new InMemoryLearnerRepository();
		service = new WaitlistService(offerings, learners, new EligibilityPolicy());
		offerings.save(new CourseOffering(OFFERING, 2, PREREQUISITE));
	}

	@Test
	void enroll_whenSeatAvailable_enrolls() {
		eligible("A");

		EnrollResult result = service.enroll(OFFERING, "A");

		assertThat(result.outcome()).isEqualTo(EnrollOutcome.ENROLLED);
		assertThat(state().enrolled()).containsExactly("A");
		assertThat(state().seatsAvailable()).isEqualTo(1);
	}

	@Test
	void enroll_whenFull_joinsWaitlist() {
		enrollAll("A", "B");
		eligible("C");

		EnrollResult result = service.enroll(OFFERING, "C");

		assertThat(result.outcome()).isEqualTo(EnrollOutcome.WAITLISTED);
		assertThat(result.waitlistPosition()).isEqualTo(1);
		assertThat(state().enrolled()).containsExactly("A", "B");
		assertThat(state().waitlist()).containsExactly("C");
	}

	@Test
	void enroll_duplicateRequest_doesNotCreateDuplicateRecord() {
		enrollAll("A", "B", "C");
		OfferingState before = state();

		assertThat(service.enroll(OFFERING, "A").outcome()).isEqualTo(EnrollOutcome.ALREADY_ENROLLED);
		assertThat(service.enroll(OFFERING, "C").outcome()).isEqualTo(EnrollOutcome.ALREADY_WAITLISTED);
		assertThat(state()).isEqualTo(before);
	}

	@Test
	void enroll_prerequisiteNotMet_isRejectedAndNotWaitlisted() {
		enrollAll("A", "B");
		ineligible("X");
		OfferingState before = state();

		EnrollResult result = service.enroll(OFFERING, "X");

		assertThat(result.outcome()).isEqualTo(EnrollOutcome.PREREQUISITE_NOT_MET);
		assertThat(state()).isEqualTo(before);
	}

	@Test
	void drop_promotesWaitlistInFifoOrder() {
		enrollAll("A", "B", "C", "D", "E");

		DropResult firstDrop = service.drop(OFFERING, "A");
		DropResult secondDrop = service.drop(OFFERING, "B");

		assertThat(firstDrop.outcome()).isEqualTo(DropOutcome.DROPPED);
		assertThat(firstDrop.promotedLearnerId()).isEqualTo("C");
		assertThat(secondDrop.promotedLearnerId()).isEqualTo("D");
		assertThat(state().enrolled()).containsExactly("C", "D");
		assertThat(state().waitlist()).containsExactly("E");
	}

	@Test
	void drop_skipsWaitlistedLearnerWhoIsNoLongerEligible() {
		enrollAll("A", "B", "C", "D");
		ineligible("C");

		DropResult result = service.drop(OFFERING, "A");

		assertThat(result.skippedLearnerIds()).containsExactly("C");
		assertThat(result.promotedLearnerId()).isEqualTo("D");
		assertThat(state().enrolled()).containsExactly("B", "D");
		assertThat(state().waitlist()).isEmpty();
	}

	@Test
	void drop_whenNotEnrolled_returnsNotEnrolled() {
		enrollAll("A");
		eligible("B");
		OfferingState before = state();

		DropResult result = service.drop(OFFERING, "B");

		assertThat(result.outcome()).isEqualTo(DropOutcome.NOT_ENROLLED);
		assertThat(state()).isEqualTo(before);
	}

	@Test
	void drop_twice_secondDropChangesNothing() {
		enrollAll("A", "B", "C");
		service.drop(OFFERING, "A");
		OfferingState afterFirstDrop = state();

		DropResult second = service.drop(OFFERING, "A");

		assertThat(second.outcome()).isEqualTo(DropOutcome.NOT_ENROLLED);
		assertThat(state()).isEqualTo(afterFirstDrop);
	}

	@Test
	void unknownOfferingOrLearner_isRejectedWithoutChangingState() {
		enrollAll("A");
		OfferingState before = state();

		assertThatThrownBy(() -> service.enroll("MISSING", "A")).isInstanceOf(OfferingNotFoundException.class);
		assertThatThrownBy(() -> service.enroll(OFFERING, "MISSING")).isInstanceOf(LearnerNotFoundException.class);
		assertThatThrownBy(() -> service.drop(OFFERING, "MISSING")).isInstanceOf(LearnerNotFoundException.class);
		assertThat(state()).isEqualTo(before);
	}

	private OfferingState state() {
		return service.getState(OFFERING);
	}

	private void eligible(String learnerId) {
		learners.save(new Learner(learnerId, Set.of(PREREQUISITE)));
	}

	private void ineligible(String learnerId) {
		learners.save(new Learner(learnerId, Set.of()));
	}

	private void enrollAll(String... learnerIds) {
		for (String learnerId : learnerIds) {
			eligible(learnerId);
			service.enroll(OFFERING, learnerId);
		}
	}
}
