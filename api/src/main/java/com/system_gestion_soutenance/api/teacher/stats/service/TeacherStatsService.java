package com.system_gestion_soutenance.api.teacher.stats.service;

import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.unavailability.repository.UnavailabilityRepository;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.EvaluationStatus;
import com.system_gestion_soutenance.api.teacher.evaluation.repository.EvaluationRepository;
import com.system_gestion_soutenance.api.teacher.stats.dto.TeacherStatsResponse;
import org.springframework.stereotype.Service;

@Service
public class TeacherStatsService {

	private final EvaluationRepository evaluationRepository;
	private final DefenseRepository defenseRepository;
	private final UnavailabilityRepository unavailabilityRepository;

	public TeacherStatsService(EvaluationRepository evaluationRepository, DefenseRepository defenseRepository,
			UnavailabilityRepository unavailabilityRepository) {
		this.evaluationRepository = evaluationRepository;
		this.defenseRepository = defenseRepository;
		this.unavailabilityRepository = unavailabilityRepository;
	}

	public TeacherStatsResponse getStats(Long teacherId) {
		long juryCount = defenseRepository.countByMembers_Teacher_Id(teacherId);

		return new TeacherStatsResponse(0,
				evaluationRepository.findByTeacherId(teacherId).stream()
						.filter(e -> e.getStatus() == EvaluationStatus.PENDING).count(),
				unavailabilityRepository.findAll().stream().filter(u -> u.getTeacherId().equals(teacherId))
						.mapToLong(u -> u.getSlots() != null ? u.getSlots().size() : 0).sum(),
				juryCount);
	}
}
