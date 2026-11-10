package com.company.app.coursewaitlist.config;

import java.util.List;
import java.util.Set;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.company.app.coursewaitlist.model.CourseOffering;
import com.company.app.coursewaitlist.model.Learner;
import com.company.app.coursewaitlist.repository.LearnerRepository;
import com.company.app.coursewaitlist.repository.OfferingRepository;

/** Sample data for running the app by hand. Service tests build their own data. */
@Component
public class SeedData implements ApplicationRunner {

	private static final String OFFERING_ID = "CS201-FALL-2026";
	private static final int CAPACITY = 2;
	private static final String PREREQUISITE = "CS101";

	private final OfferingRepository offerings;
	private final LearnerRepository learners;

	public SeedData(OfferingRepository offerings, LearnerRepository learners) {
		this.offerings = offerings;
		this.learners = learners;
	}

	@Override
	public void run(ApplicationArguments args) {
		offerings.save(new CourseOffering(OFFERING_ID, CAPACITY, PREREQUISITE));

		for (String learnerId : List.of("L1", "L2", "L3", "L5", "L6")) {
			learners.save(new Learner(learnerId, Set.of(PREREQUISITE)));
		}
		learners.save(new Learner("L4", Set.of())); // has not met the prerequisite
	}
}
