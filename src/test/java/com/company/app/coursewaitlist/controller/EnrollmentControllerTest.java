package com.company.app.coursewaitlist.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Runs against the seed data: capacity 2, prerequisite CS101, L4 ineligible. */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EnrollmentControllerTest {

	private static final String ENROLLMENTS_URL = "/offerings/CS201-FALL-2026/enrollments";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void enroll_withFreeSeat_returns201() throws Exception {
		enroll("L1")
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.outcome").value("ENROLLED"));
	}

	@Test
	void enroll_whenPrerequisiteNotMet_returns422() throws Exception {
		enroll("L4")
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.outcome").value("PREREQUISITE_NOT_MET"));
	}

	@Test
	void drop_enrolledLearner_returns200AndPromotes() throws Exception {
		enroll("L1");
		enroll("L2");
		enroll("L3");

		drop("L1")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.outcome").value("DROPPED"))
			.andExpect(jsonPath("$.promoted").value("L3"));
	}

	@Test
	void drop_whenNotEnrolled_returns409() throws Exception {
		drop("L5")
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.outcome").value("NOT_ENROLLED"));
	}

	@Test
	void enroll_unknownLearner_returns404() throws Exception {
		enroll("L99")
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("LEARNER_NOT_FOUND"));
	}

	@Test
	void enroll_blankLearnerId_returns400() throws Exception {
		mockMvc.perform(post(ENROLLMENTS_URL).contentType(MediaType.APPLICATION_JSON).content("{\"learnerId\":\"\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
	}

	private ResultActions enroll(String learnerId) throws Exception {
		return mockMvc.perform(post(ENROLLMENTS_URL).contentType(MediaType.APPLICATION_JSON)
			.content("{\"learnerId\":\"" + learnerId + "\"}"));
	}

	private ResultActions drop(String learnerId) throws Exception {
		return mockMvc.perform(delete(ENROLLMENTS_URL + "/" + learnerId));
	}
}
