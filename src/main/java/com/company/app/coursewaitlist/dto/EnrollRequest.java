package com.company.app.coursewaitlist.dto;

import jakarta.validation.constraints.NotBlank;

public record EnrollRequest(@NotBlank String learnerId) {
}
