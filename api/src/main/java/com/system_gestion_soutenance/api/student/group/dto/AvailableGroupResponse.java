package com.system_gestion_soutenance.api.student.group.dto;
@SuppressWarnings("PMD")

public record AvailableGroupResponse(Long id, String groupName, int memberCount) {
}