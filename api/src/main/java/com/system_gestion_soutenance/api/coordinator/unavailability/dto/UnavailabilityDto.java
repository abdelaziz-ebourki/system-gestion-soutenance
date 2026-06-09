package com.system_gestion_soutenance.api.coordinator.unavailability.dto;

import java.util.List;
@SuppressWarnings("PMD")

public record UnavailabilityDto(Long id, Long teacherId, String date, List<String> slots) {
}