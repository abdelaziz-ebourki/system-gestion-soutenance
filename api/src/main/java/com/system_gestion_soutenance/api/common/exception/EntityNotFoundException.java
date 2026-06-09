package com.system_gestion_soutenance.api.common.exception;

import org.springframework.http.HttpStatus;
@SuppressWarnings("PMD")

public class EntityNotFoundException extends BaseBusinessException {
	public EntityNotFoundException(String message) {
		super(message, HttpStatus.NOT_FOUND);
	}
}