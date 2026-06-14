package com.system_gestion_soutenance.api.coordinator.project.dto;

import java.util.List;

public record BulkImportResult(int total, int imported, List<BulkProjectResponse> created,
		List<BulkImportError> errors) {

	public record BulkImportError(int line, String message) {
	}
}
