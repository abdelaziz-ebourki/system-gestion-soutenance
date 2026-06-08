package com.system_gestion_soutenance.api.coordinator.jury.dto;

import java.util.List;

public record UpdateJuryRequest(Long projectId, List<MemberEntry> members) {
	public record MemberEntry(Long teacherId, String roleName) {
	}
}
