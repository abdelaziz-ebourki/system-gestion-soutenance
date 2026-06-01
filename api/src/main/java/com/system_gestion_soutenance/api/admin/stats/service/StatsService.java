package com.system_gestion_soutenance.api.admin.stats.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StatsService {

	private final EntityManager entityManager;

	public StatsService(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public Map<String, Object> getStats() {
		Query query = entityManager.createNativeQuery("""
				SELECT
				  (SELECT COUNT(*) FROM users WHERE dtype = 'STUDENT') AS total_students,
				  (SELECT COUNT(*) FROM users WHERE dtype = 'TEACHER') AS total_teachers,
				  (SELECT COUNT(*) FROM department) AS total_departments,
				  (SELECT COUNT(*) FROM room) AS total_rooms,
				  (SELECT COUNT(*) FROM defense_session) AS total_defense_sessions
				""");

		Object[] row = (Object[]) query.getSingleResult();

		Map<String, Object> stats = new HashMap<>();
		stats.put("totalStudents", row[0]);
		stats.put("totalTeachers", row[1]);
		stats.put("totalDepartments", row[2]);
		stats.put("totalRooms", row[3]);
		stats.put("totalDefenseSessions", row[4]);
		return stats;
	}
}
