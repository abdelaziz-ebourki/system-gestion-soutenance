package com.system_gestion_soutenance.api.student.group.dto;
@SuppressWarnings("PMD")

public record GroupMemberResponse(Long id, String fullName, String email, String role) {
}