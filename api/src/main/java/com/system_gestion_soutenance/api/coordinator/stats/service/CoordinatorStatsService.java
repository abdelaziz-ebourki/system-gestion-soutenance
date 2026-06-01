package com.system_gestion_soutenance.api.coordinator.stats.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CoordinatorStatsService {

	private final EntityManager entityManager;

	public CoordinatorStatsService(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public Map<String, Object> getStats() {
		Query query = entityManager.createNativeQuery("""
				SELECT
				  (SELECT COUNT(*) FROM project) AS total_projects,
				  (SELECT COUNT(*) FROM coordinator_group) AS total_groups,
				  (SELECT COUNT(*) FROM jury) AS total_juries,
				  (SELECT COUNT(*) FROM defense_session) AS scheduled_defenses
				""");

		Object[] row = (Object[]) query.getSingleResult();

		Map<String, Object> stats = new HashMap<>();
		stats.put("totalProjects", row[0]);
		stats.put("totalGroups", row[1]);
		stats.put("totalJuries", row[2]);
		stats.put("scheduledDefenses", row[3]);
		return stats;
	}
}
