package com.system_gestion_soutenance.api.student.group.dto;

import java.util.List;
@SuppressWarnings("PMD")

public record GroupDetailsResponse(Long id, String groupName, String projectTitle, String supervisorName,
		List<GroupMemberResponse> members) {
}