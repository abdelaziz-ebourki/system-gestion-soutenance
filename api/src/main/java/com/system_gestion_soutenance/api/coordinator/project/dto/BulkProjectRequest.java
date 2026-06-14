package com.system_gestion_soutenance.api.coordinator.project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BulkProjectRequest(
		@NotEmpty(message = "La liste des projets ne peut être vide") @Valid List<BulkProjectEntry> projects) {
}
