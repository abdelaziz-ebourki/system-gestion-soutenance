package com.system_gestion_soutenance.api.admin.config.juryrole.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "Request to create a new jury role template")
public record CreateJuryRoleTemplateRequest(
		@Schema(description = "Name of the jury role template", example = "Standard CDM Template") @NotBlank String name,
		@Schema(description = "Type of defense this template applies to", example = "CDM") @NotBlank String defenseType,
		@Schema(description = "List of roles defined in the template") @NotEmpty List<RoleEntry> roles) {

	@Schema(description = "Details of a single role in the template")
	public record RoleEntry(@Schema(description = "Name of the role", example = "President") @NotBlank String name,
			@Schema(description = "Number of people for this role", example = "1") int count,
			@Schema(description = "Coefficient for evaluation", example = "1") int coefficient) {
	}
}
