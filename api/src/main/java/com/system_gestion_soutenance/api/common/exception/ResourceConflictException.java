package com.system_gestion_soutenance.api.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceConflictException extends BaseBusinessException {
	public ResourceConflictException(String message) {
		super(message, HttpStatus.CONFLICT);
	}
}
