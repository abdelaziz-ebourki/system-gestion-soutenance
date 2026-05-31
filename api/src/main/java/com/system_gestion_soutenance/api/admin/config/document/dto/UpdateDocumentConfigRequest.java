package com.system_gestion_soutenance.api.admin.config.document.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateDocumentConfigRequest(@Min(1) int maxFileSizeMb, @NotBlank String allowedExtensions,
		@Min(1) int versionLimit) {
}
