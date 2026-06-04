package com.system_gestion_soutenance.api.coordinator.jury.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateJuryRequest(@NotNull(message = "Le projet est obligatoire") Long projectId,
		@NotNull(message = "Le template est obligatoire") Long templateId,
		@NotNull(message = "L'équipe du jury est obligatoire") List<MemberEntry> members) {
	public record MemberEntry(@NotNull(message = "L'enseignant est obligatoire") Long teacherId,
			@NotNull(message = "Le rôle est obligatoire") String roleName) {
	}
}
