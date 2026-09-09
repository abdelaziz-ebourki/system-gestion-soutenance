package com.system_gestion_soutenance.api.admin.stats.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing global statistics")
@SuppressWarnings("PMD")
public record GlobalStatsResponse(@Schema(description = "Total number of students", example = "500") long totalStudents,
		@Schema(description = "Total number of teachers", example = "50") long totalTeachers,
		@Schema(description = "Total number of departments", example = "10") long totalDepartments,
		@Schema(description = "Total number of rooms", example = "20") long totalRooms,
		@Schema(description = "Total number of defense sessions", example = "15") long totalDefenseSessions) {
}
