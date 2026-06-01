package com.system_gestion_soutenance.api.student.defense.service;

import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.jury.entity.Jury;
import com.system_gestion_soutenance.api.coordinator.jury.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.schedule.entity.SlotAssignment;
import com.system_gestion_soutenance.api.coordinator.schedule.repository.SlotAssignmentRepository;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StudentDefenseService {

	private final GroupRepository groupRepository;
	private final JuryRepository juryRepository;
	private final SlotAssignmentRepository slotAssignmentRepository;

	public StudentDefenseService(GroupRepository groupRepository, JuryRepository juryRepository,
			SlotAssignmentRepository slotAssignmentRepository) {
		this.groupRepository = groupRepository;
		this.juryRepository = juryRepository;
		this.slotAssignmentRepository = slotAssignmentRepository;
	}

	@Transactional(readOnly = true)
	public Map<String, Object> getDefense(Long studentId) {
		Group group = groupRepository.findByStudentId(studentId).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucune soutenance trouvée pour cet étudiant"));

		if (group.getProject() == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucun projet associé à ce groupe");
		}

		Project project = group.getProject();
		Map<String, Object> defense = new LinkedHashMap<>();
		defense.put("projectTitle", project.getTitle());
		defense.put("projectDescription", project.getDescription());

		defense.put("supervisorName",
				project.getSupervisor() != null
						? project.getSupervisor().getFirstName() + " " + project.getSupervisor().getLastName()
						: null);

		List<Map<String, String>> juryMembers = new ArrayList<>();
		for (Jury jury : juryRepository.findByProjectId(project.getId())) {
			for (JuryMember member : jury.getMembers()) {
				if (member.getTeacher() != null) {
					Map<String, String> m = new LinkedHashMap<>();
					m.put("name", member.getTeacher().getFirstName() + " " + member.getTeacher().getLastName());
					m.put("role", member.getRoleName());
					juryMembers.add(m);
				}
			}
		}
		defense.put("juryMembers", juryMembers);

		List<SlotAssignment> slots = slotAssignmentRepository.findByProjectId(project.getId());
		if (!slots.isEmpty()) {
			SlotAssignment slot = slots.get(0);
			defense.put("date", slot.getDate());
			defense.put("startTime", slot.getTime());
			defense.put("endTime", "");
			defense.put("roomName", slot.getRoom() != null ? slot.getRoom().getName() : "");
			defense.put("status", "scheduled");
		} else {
			defense.put("status", "pending");
		}

		defense.put("convocationUrl", null);
		defense.put("result", null);

		return defense;
	}
}
