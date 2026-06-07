package com.system_gestion_soutenance.api.student.defense.service;

import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.jury.entity.Jury;
import com.system_gestion_soutenance.api.coordinator.jury.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.schedule.entity.SlotAssignment;
import com.system_gestion_soutenance.api.coordinator.schedule.repository.SlotAssignmentRepository;
import com.system_gestion_soutenance.api.student.defense.dto.JuryMemberResponse;
import com.system_gestion_soutenance.api.student.defense.dto.StudentDefenseResponse;
import java.util.*;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	public StudentDefenseResponse getDefense(Long studentId) {
		Group group = groupRepository.findByStudentId(studentId)
				.orElseThrow(() -> new EntityNotFoundException("Aucune soutenance trouvée pour cet étudiant"));

		if (group.getProject() == null) {
			throw new EntityNotFoundException("Aucun projet associé à ce groupe");
		}

		Project project = group.getProject();

		List<JuryMemberResponse> juryMembers = new ArrayList<>();
		for (Jury jury : juryRepository.findByProjectId(project.getId())) {
			for (JuryMember member : jury.getMembers()) {
				if (member.getTeacher() != null) {
					juryMembers.add(new JuryMemberResponse(
							member.getTeacher().getFirstName() + " " + member.getTeacher().getLastName(),
							member.getRoleName()));
				}
			}
		}

		String date = null;
		String startTime = null;
		String roomName = null;
		String status = "pending";

		List<SlotAssignment> slots = slotAssignmentRepository.findByProjectId(project.getId());
		if (!slots.isEmpty()) {
			SlotAssignment slot = slots.get(0);
			date = slot.getDate();
			startTime = slot.getTime();
			roomName = slot.getRoom() != null ? slot.getRoom().getName() : "";
			status = "scheduled";
		}

		return new StudentDefenseResponse(project.getTitle(), project.getDescription(),
				project.getSupervisor() != null
						? project.getSupervisor().getFirstName() + " " + project.getSupervisor().getLastName()
						: null,
				juryMembers, date, startTime, "", roomName, status, null, null);
	}
}
