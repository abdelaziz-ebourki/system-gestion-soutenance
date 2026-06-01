package com.system_gestion_soutenance.api.coordinator.unavailability.dto;

import java.util.List;

public record UnavailabilityDto(Long id, Long teacherId, String date, List<String> slots) {
}
