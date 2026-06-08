package com.system_gestion_soutenance.api.student.stats.service;

import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.student.document.entity.StudentDocument;
import com.system_gestion_soutenance.api.student.document.repository.StudentDocumentRepository;
import com.system_gestion_soutenance.api.student.stats.dto.StudentStatsResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentStatsService {

	private final StudentDocumentRepository documentRepository;
	private final GroupRepository groupRepository;
	private final DefenseRepository defenseRepository;

	public StudentStatsService(StudentDocumentRepository documentRepository, GroupRepository groupRepository,
			DefenseRepository defenseRepository) {
		this.documentRepository = documentRepository;
		this.groupRepository = groupRepository;
		this.defenseRepository = defenseRepository;
	}

	@Transactional(readOnly = true)
	public StudentStatsResponse getStats(Long studentId) {
		List<StudentDocument> docs = documentRepository.findByStudentId(studentId);
		long missing = docs.stream().filter(d -> "missing".equals(d.getStatus())).count();

		Group group = groupRepository.findByStudentId(studentId).orElse(null);
		int groupMembers = group != null ? group.getStudents().size() : 0;
		Long projectId = (group != null && group.getProject() != null) ? group.getProject().getId() : null;

		boolean hasSchedule = projectId != null && defenseRepository.existsByProject_Id(projectId);

		return new StudentStatsResponse(docs.size(), missing, groupMembers, hasSchedule ? "scheduled" : "pending");
	}
}
