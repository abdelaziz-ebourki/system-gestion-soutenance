package com.system_gestion_soutenance.api.coordinator.document.dto;

import java.util.List;

public record SlotDetails(String date, String time, String roomName, String projectTitle, List<String> studentNames) {
}
