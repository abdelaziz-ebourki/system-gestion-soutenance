package com.system_gestion_soutenance.api.common.exception;

import org.springframework.http.HttpStatus;
@SuppressWarnings("PMD")

public class ResultsNotPublishedException extends BaseBusinessException {
	public ResultsNotPublishedException(String message) {
		super(message, HttpStatus.FORBIDDEN);
	}
}
