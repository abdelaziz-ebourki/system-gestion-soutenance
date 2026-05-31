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

	public Map<String, Object> getStats(Long studentId) {
		List<StudentDocument> docs = documentRepository.findByStudentId(studentId);
		long missing = docs.stream().filter(d -> "missing".equals(d.getStatus())).count();

		Long projectId = null;
		int groupMembers = 0;
		for (Group g : groupRepository.findAll()) {
			if (g.getStudents() != null && g.getStudents().stream().anyMatch(s -> s.getId().equals(studentId))) {
				groupMembers = g.getStudents().size();
				if (g.getProject() != null)
					projectId = g.getProject().getId();
				break;
			}
		}

		final Long pid = projectId;
		boolean hasSchedule = pid != null
				&& slotAssignmentRepository.findAll().stream().anyMatch(s -> pid.equals(s.getProjectId()));

		Map<String, Object> stats = new HashMap<>();
		stats.put("documentCount", docs.size());
		stats.put("missingDocuments", missing);
		stats.put("groupMembers", groupMembers);
		stats.put("defenseStatus", hasSchedule ? "scheduled" : "pending");
		return stats;
	}
}
