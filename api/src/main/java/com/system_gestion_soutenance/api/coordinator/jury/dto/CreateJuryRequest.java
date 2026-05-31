package com.system_gestion_soutenance.api.coordinator.jury.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateJuryRequest(@NotNull Long projectId, @NotNull Long templateId,
		@NotEmpty List<MemberEntry> members) {
	public record MemberEntry(@NotNull Long teacherId, @NotBlank String roleName) {
	}
}
