package com.system_gestion_soutenance.api.coordinator.stats.service;

import com.system_gestion_soutenance.api.coordinator.stats.dto.CoordinatorStatsResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;

@Service
public class CoordinatorStatsService {

	private final EntityManager entityManager;

	public CoordinatorStatsService(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public CoordinatorStatsResponse getStats() {
		Query query = entityManager.createNativeQuery("""
				SELECT
				  (SELECT COUNT(*) FROM project) AS total_projects,
				  (SELECT COUNT(*) FROM coordinator_group) AS total_groups,
				  (SELECT COUNT(*) FROM jury) AS total_juries,
				  (SELECT COUNT(*) FROM defense_session) AS scheduled_defenses
				""");

		Object[] row = (Object[]) query.getSingleResult();

		return new CoordinatorStatsResponse(((Number) row[0]).longValue(), ((Number) row[1]).longValue(),
				((Number) row[2]).longValue(), ((Number) row[3]).longValue());
	}
}
