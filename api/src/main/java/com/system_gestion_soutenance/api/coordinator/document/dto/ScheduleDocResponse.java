package com.system_gestion_soutenance.api.coordinator.document.dto;

import java.util.List;
@SuppressWarnings("PMD")

public record ScheduleDocResponse(String defenseSessionName, List<SlotDetails> slots) {
}