package com.system_gestion_soutenance.api.admin.defensesession.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Response containing defense session details")
@SuppressWarnings("PMD")
public record DefenseSessionDto(
		@Schema(description = "Unique identifier of the defense session", example = "1") Long id,
		@Schema(description = "Name of the defense session", example = "S1 CDM 2026") String name,
		@Schema(description = "Type of defense", example = "CDM") String defenseType,
		@Schema(description = "Current status of the session", example = "ACTIVE") String status,
		@Schema(description = "Maximum number of students per group", example = "5") int maxGroupSize,
		@Schema(description = "Duration of each defense in minutes", example = "30") int defenseDuration,
		@Schema(description = "Duration of breaks between defenses in minutes", example = "10") int breakDuration,
		@Schema(description = "Deadline for student submissions", example = "2026-02-01") LocalDate submissionDeadline,
		@Schema(description = "Map of evaluation criteria and their coefficients") Map<String, Integer> evaluationCoefficients,
		@Schema(description = "ID of the jury role template used", example = "1") Long juryRoleTemplateId,
		@Schema(description = "Start date of the defense session", example = "2026-03-01") LocalDate startDate,
		@Schema(description = "End date of the defense session", example = "2026-03-15") LocalDate endDate,
		@Schema(description = "Whether the session is frozen", example = "false") boolean isFrozen,
		@Schema(description = "Allow supervisor in jury", example = "false") boolean allowSupervisorInJury,
		@Schema(description = "ID of the user who approved the session", example = "1") Long approvedBy,
		@Schema(description = "Timestamp when the session was approved", example = "2026-01-20T14:00:00") LocalDateTime approvedAt,
		@Schema(description = "Start time for defense slots", example = "08:00") String startTime,
		@Schema(description = "End time for defense slots", example = "18:00") String endTime,
		@Schema(description = "Group creation period start date", example = "2026-03-01") String groupCreationStartDate,
		@Schema(description = "Group creation period end date", example = "2026-05-01") String groupCreationEndDate) {
}
