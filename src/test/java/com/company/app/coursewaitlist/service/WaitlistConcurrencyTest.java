package com.company.app.coursewaitlist.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

import com.company.app.coursewaitlist.model.CourseOffering;
import com.company.app.coursewaitlist.model.EnrollOutcome;
import com.company.app.coursewaitlist.model.EnrollResult;
import com.company.app.coursewaitlist.model.Learner;
import com.company.app.coursewaitlist.model.OfferingState;
import com.company.app.coursewaitlist.repository.InMemoryLearnerRepository;
import com.company.app.coursewaitlist.repository.InMemoryOfferingRepository;
import com.company.app.coursewaitlist.repository.LearnerRepository;
import com.company.app.coursewaitlist.repository.OfferingRepository;

class WaitlistConcurrencyTest {

	private static final String OFFERING = "OFFERING-1";
	private static final String PREREQUISITE = "CS101";

	@Test
	void concurrentEnroll_neverExceedsCapacity() throws Exception {
		OfferingRepository offerings = new InMemoryOfferingRepository();
		LearnerRepository learners = new InMemoryLearnerRepository();
		offerings.save(new CourseOffering(OFFERING, 5, PREREQUISITE));
		WaitlistService service = new WaitlistService(offerings, learners, new EligibilityPolicy());

		int requests = 50;
		CountDownLatch startGate = new CountDownLatch(1);
		ExecutorService pool = Executors.newFixedThreadPool(requests);
		List<Future<EnrollResult>> futures = new ArrayList<>();
		for (int i = 1; i <= requests; i++) {
			String learnerId = "L" + i;
			learners.save(new Learner(learnerId, Set.of(PREREQUISITE)));
			futures.add(pool.submit(() -> {
				startGate.await(); // all threads wait here, then enroll at the same moment
				return service.enroll(OFFERING, learnerId);
			}));
		}
		startGate.countDown();

		int enrolled = 0;
		int waitlisted = 0;
		for (Future<EnrollResult> future : futures) {
			EnrollOutcome outcome = future.get().outcome();
			if (outcome == EnrollOutcome.ENROLLED) {
				enrolled++;
			}
			if (outcome == EnrollOutcome.WAITLISTED) {
				waitlisted++;
			}
		}
		pool.shutdown();

		assertThat(enrolled).isEqualTo(5);
		assertThat(waitlisted).isEqualTo(45);
		OfferingState state = service.getState(OFFERING);
		assertThat(state.enrolled()).hasSize(5).doesNotHaveDuplicates();
		assertThat(state.waitlist()).hasSize(45).doesNotHaveDuplicates();
	}
}
