package com.system_gestion_soutenance.api.common.exception;

import org.springframework.http.HttpStatus;
@SuppressWarnings("PMD")

public class InvalidBusinessStateException extends BaseBusinessException {
	public InvalidBusinessStateException(String message) {
		super(message, HttpStatus.BAD_REQUEST);
	}

	public InvalidBusinessStateException(String message, Throwable cause) {
		super(message, cause, HttpStatus.BAD_REQUEST);
	}
}