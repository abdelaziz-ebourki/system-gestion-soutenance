package com.system_gestion_soutenance.api.teacher.unavailability.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record TeacherUnavailabilityRequest(
		@NotEmpty(message = "Au moins un créneau d'indisponibilité est requis") List<UnavailabilitySlotRequest> slots) {
}
