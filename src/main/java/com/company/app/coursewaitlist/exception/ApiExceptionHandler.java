package com.company.app.coursewaitlist.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.company.app.coursewaitlist.dto.ErrorResponse;


@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(OfferingNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleOfferingNotFound(OfferingNotFoundException exception) {
		return new ErrorResponse("OFFERING_NOT_FOUND", exception.getMessage());
	}

	@ExceptionHandler(LearnerNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleLearnerNotFound(LearnerNotFoundException exception) {
		return new ErrorResponse("LEARNER_NOT_FOUND", exception.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleInvalidBody(MethodArgumentNotValidException exception) {
		return new ErrorResponse("INVALID_REQUEST", "learnerId must not be blank");
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleUnreadableBody(HttpMessageNotReadableException exception) {
		return new ErrorResponse("INVALID_REQUEST", "Request body is missing or is not valid JSON");
	}
}
