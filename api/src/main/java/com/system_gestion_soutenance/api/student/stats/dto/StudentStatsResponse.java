package com.system_gestion_soutenance.api.student.stats.dto;

public record StudentStatsResponse(int documentCount, long missingDocuments, int groupMembers, String defenseStatus) {
}
