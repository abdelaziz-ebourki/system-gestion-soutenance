package com.system_gestion_soutenance.api.coordinator.config.dto;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing defense session settings")
public record DefenseSettingsResponse(
		@Schema(description = "Start time for defense slots", example = "08:00") String startTime,
		@Schema(description = "End time for defense slots", example = "18:00") String endTime,
		@Schema(description = "Duration of each defense in minutes", example = "30") int defenseDuration,
		@Schema(description = "Duration of breaks between defenses in minutes", example = "10") int breakDuration,
		@Schema(description = "Group creation period start date", example = "2026-03-01") String groupCreationStartDate,
		@Schema(description = "Group creation period end date", example = "2026-05-01") String groupCreationEndDate) {

	public static DefenseSettingsResponse from(DefenseSession session) {
		return new DefenseSettingsResponse(session.getStartTime(), session.getEndTime(), session.getDefenseDuration(),
				session.getBreakDuration(), session.getGroupCreationStartDate(), session.getGroupCreationEndDate());
	}
}
