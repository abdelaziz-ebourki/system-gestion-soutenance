package com.system_gestion_soutenance.api.admin.stats.service;

import com.system_gestion_soutenance.api.admin.stats.dto.GlobalStatsResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StatsService {

	private final EntityManager entityManager;

	public StatsService(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public GlobalStatsResponse getStats() {
		Query query = entityManager.createNativeQuery("""
				SELECT
				  (SELECT COUNT(*) FROM users WHERE dtype = 'STUDENT') AS total_students,
				  (SELECT COUNT(*) FROM users WHERE dtype = 'TEACHER') AS total_teachers,
				  (SELECT COUNT(*) FROM department) AS total_departments,
				  (SELECT COUNT(*) FROM room) AS total_rooms,
				  (SELECT COUNT(*) FROM defense_session) AS total_defense_sessions
				""");

		Object[] row = (Object[]) query.getSingleResult();

		return new GlobalStatsResponse(((Number) row[0]).longValue(), ((Number) row[1]).longValue(),
				((Number) row[2]).longValue(), ((Number) row[3]).longValue(), ((Number) row[4]).longValue());
	}

}
