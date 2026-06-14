package com.system_gestion_soutenance.api.admin.config.juryrole.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Response containing jury role template details")
@SuppressWarnings("PMD")
public record JuryRoleTemplateDto(@Schema(description = "Unique identifier of the template", example = "1") Long id,
		@Schema(description = "Name of the jury role template", example = "Standard CDM Template") String name,
		@Schema(description = "Type of defense this template applies to", example = "CDM") String defenseType,
		@Schema(description = "List of roles defined in the template") List<TemplateRoleDto> roles) {
}
