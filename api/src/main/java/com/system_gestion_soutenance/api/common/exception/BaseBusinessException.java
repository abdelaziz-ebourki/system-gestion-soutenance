package com.system_gestion_soutenance.api.common.exception;

import org.springframework.http.HttpStatus;
@SuppressWarnings("PMD")

public abstract class BaseBusinessException extends RuntimeException {
	private final HttpStatus status;

	protected BaseBusinessException(String message, HttpStatus status) {
		super(message);
		this.status = status;
	}

	protected BaseBusinessException(String message, Throwable cause, HttpStatus status) {
		super(message, cause);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}