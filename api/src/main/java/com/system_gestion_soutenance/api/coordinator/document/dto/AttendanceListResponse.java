package com.system_gestion_soutenance.api.coordinator.document.dto;

import java.util.List;

public record AttendanceListResponse(String defenseSessionName, List<SlotDetails> slots) {
}
