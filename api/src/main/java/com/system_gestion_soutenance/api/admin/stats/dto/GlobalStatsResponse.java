package com.system_gestion_soutenance.api.admin.stats.dto;
@SuppressWarnings("PMD")

public record GlobalStatsResponse(long totalStudents, long totalTeachers, long totalDepartments, long totalRooms,
		long totalDefenseSessions) {
}