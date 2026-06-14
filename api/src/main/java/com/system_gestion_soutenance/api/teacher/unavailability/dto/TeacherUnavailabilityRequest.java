package com.system_gestion_soutenance.api.teacher.unavailability.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "Requête d'indisponibilité de l'enseignant contenant plusieurs créneaux")
@SuppressWarnings("PMD")
public record TeacherUnavailabilityRequest(
		@Schema(description = "Liste des créneaux d'indisponibilité") @NotEmpty(message = "Au moins un créneau d'indisponibilité est requis") List<UnavailabilitySlotRequest> slots) {
}
