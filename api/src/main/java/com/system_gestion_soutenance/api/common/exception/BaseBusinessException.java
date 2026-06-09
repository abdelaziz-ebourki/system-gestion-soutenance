package com.system_gestion_soutenance.api.common.exception;

import org.springframework.http.HttpStatus;
@SuppressWarnings("PMD")

public abstract class BaseBusinessException extends RuntimeException {
	private final HttpStatus status;

	protected BaseBusinessException(String message, HttpStatus status) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}