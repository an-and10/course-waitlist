package com.company.app.coursewaitlist.service;

import org.springframework.stereotype.Component;

import com.company.app.coursewaitlist.model.CourseOffering;
import com.company.app.coursewaitlist.model.Learner;

/** The single prerequisite rule: one completed course per offering, or none. */
@Component
public class EligibilityPolicy {

	public boolean isEligible(Learner learner, CourseOffering offering) {
		String prerequisite = offering.getPrerequisiteCourseId();
		return prerequisite == null || learner.hasCompleted(prerequisite);
	}
}
