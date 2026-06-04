package com.system_gestion_soutenance.api.teacher.stats.service;

import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryMemberRepository;
import com.system_gestion_soutenance.api.coordinator.unavailability.repository.UnavailabilityRepository;
import com.system_gestion_soutenance.api.teacher.evaluation.repository.EvaluationRepository;
import com.system_gestion_soutenance.api.teacher.stats.dto.TeacherStatsResponse;
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

	public TeacherStatsResponse getStats(Long teacherId) {
		return new TeacherStatsResponse(0,
				evaluationRepository.findByTeacherId(teacherId).stream().filter(e -> "pending".equals(e.getStatus()))
						.count(),
				unavailabilityRepository.findAll().stream().filter(u -> u.getTeacherId().equals(teacherId))
						.mapToLong(u -> u.getSlots() != null ? u.getSlots().size() : 0).sum(),
				(long) juryMemberRepository.findByTeacher_Id(teacherId).size());
	}
}
