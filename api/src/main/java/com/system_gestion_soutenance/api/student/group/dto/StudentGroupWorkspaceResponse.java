package com.system_gestion_soutenance.api.student.group.dto;

import java.util.List;
@SuppressWarnings("PMD")

public record StudentGroupWorkspaceResponse(GroupDetailsResponse currentGroup,
		List<AvailableGroupResponse> availableGroups, String groupCreationStartDate, String groupCreationEndDate,
		boolean isGroupCreationOpen) {
}