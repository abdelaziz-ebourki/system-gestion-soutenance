package com.system_gestion_soutenance.api.student.stats.service;

import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.schedule.repository.SlotAssignmentRepository;
import com.system_gestion_soutenance.api.student.document.entity.StudentDocument;
import com.system_gestion_soutenance.api.student.document.repository.StudentDocumentRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentStatsService {

	private final StudentDocumentRepository documentRepository;
	private final GroupRepository groupRepository;
	private final SlotAssignmentRepository slotAssignmentRepository;

	public StudentStatsService(StudentDocumentRepository documentRepository, GroupRepository groupRepository,
			SlotAssignmentRepository slotAssignmentRepository) {
		this.documentRepository = documentRepository;
		this.groupRepository = groupRepository;
		this.slotAssignmentRepository = slotAssignmentRepository;
	}

	@Transactional(readOnly = true)
	public Map<String, Object> getStats(Long studentId) {
		List<StudentDocument> docs = documentRepository.findByStudentId(studentId);
		long missing = docs.stream().filter(d -> "missing".equals(d.getStatus())).count();

		Group group = groupRepository.findByStudentId(studentId).orElse(null);
		int groupMembers = group != null ? group.getStudents().size() : 0;
		Long projectId = (group != null && group.getProject() != null) ? group.getProject().getId() : null;

		boolean hasSchedule = projectId != null && slotAssignmentRepository.existsByProjectId(projectId);

		Map<String, Object> stats = new HashMap<>();
		stats.put("documentCount", docs.size());
		stats.put("missingDocuments", missing);
		stats.put("groupMembers", groupMembers);
		stats.put("defenseStatus", hasSchedule ? "scheduled" : "pending");
		return stats;
	}
}
