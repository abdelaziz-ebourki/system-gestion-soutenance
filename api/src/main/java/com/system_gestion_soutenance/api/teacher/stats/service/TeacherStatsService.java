package com.system_gestion_soutenance.api.teacher.stats.service;

import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryMemberRepository;
import com.system_gestion_soutenance.api.coordinator.unavailability.repository.UnavailabilityRepository;
import com.system_gestion_soutenance.api.teacher.evaluation.repository.EvaluationRepository;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TeacherStatsService {

	private final EvaluationRepository evaluationRepository;
	private final JuryMemberRepository juryMemberRepository;
	private final UnavailabilityRepository unavailabilityRepository;

	public TeacherStatsService(EvaluationRepository evaluationRepository, JuryMemberRepository juryMemberRepository,
			UnavailabilityRepository unavailabilityRepository) {
		this.evaluationRepository = evaluationRepository;
		this.juryMemberRepository = juryMemberRepository;
		this.unavailabilityRepository = unavailabilityRepository;
	}

	public Map<String, Object> getStats(Long teacherId) {
		Map<String, Object> stats = new HashMap<>();
		stats.put("upcomingDefenses", 0);
		stats.put("pendingEvaluations", evaluationRepository.findByTeacherId(teacherId).stream()
				.filter(e -> "pending".equals(e.getStatus())).count());
		stats.put("declaredUnavailabilitySlots",
				unavailabilityRepository.findAll().stream().filter(u -> u.getTeacherId().equals(teacherId))
						.mapToLong(u -> u.getSlots() != null ? u.getSlots().size() : 0).sum());
		stats.put("juryAssignments", (long) juryMemberRepository.findByTeacher_Id(teacherId).size());
		return stats;
	}
}
