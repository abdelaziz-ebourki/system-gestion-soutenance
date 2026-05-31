package com.system_gestion_soutenance.api.admin.config.juryrole.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateJuryRoleTemplateRequest(@NotBlank String name, @NotBlank String defenseType,
		@NotEmpty List<RoleEntry> roles) {
	public record RoleEntry(@NotBlank String name, int count, int coefficient) {
	}
}
