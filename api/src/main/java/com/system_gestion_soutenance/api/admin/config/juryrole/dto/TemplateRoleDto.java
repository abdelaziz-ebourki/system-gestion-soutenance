package com.system_gestion_soutenance.api.admin.config.juryrole.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing template role details")
public record TemplateRoleDto(@Schema(description = "Name of the role", example = "President") String name,
		@Schema(description = "Number of people for this role", example = "1") int count,
		@Schema(description = "Coefficient for evaluation", example = "1") int coefficient) {
}
