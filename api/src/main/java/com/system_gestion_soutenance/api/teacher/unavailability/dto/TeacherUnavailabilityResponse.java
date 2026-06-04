package com.system_gestion_soutenance.api.teacher.unavailability.dto;

import java.util.List;
import java.util.Map;

public record TeacherUnavailabilityResponse(Map<String, List<String>> slotsByDate) {
}
