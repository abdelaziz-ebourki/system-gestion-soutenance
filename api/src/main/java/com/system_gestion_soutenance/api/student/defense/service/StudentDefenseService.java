package com.system_gestion_soutenance.api.student.defense.service;

import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.defense.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.student.defense.dto.JuryMemberResponse;
import com.system_gestion_soutenance.api.student.defense.dto.StudentDefenseResponse;
import java.util.*;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentDefenseService {

	private final GroupRepository groupRepository;
	private final DefenseRepository defenseRepository;

	public StudentDefenseService(GroupRepository groupRepository, DefenseRepository defenseRepository) {
		this.groupRepository = groupRepository;
		this.defenseRepository = defenseRepository;
	}

	@Transactional(readOnly = true)
	public StudentDefenseResponse getDefense(Long studentId) {
		Group group = groupRepository.findByStudentId(studentId)
				.orElseThrow(() -> new EntityNotFoundException("Aucune soutenance trouvée pour cet étudiant"));

		if (group.getProject() == null) {
			throw new EntityNotFoundException("Aucun projet associé à ce groupe");
		}

		Project project = group.getProject();

		Optional<Defense> defenseOpt = defenseRepository.findByProject(project);

		List<JuryMemberResponse> juryMembers = new ArrayList<>();
		defenseOpt.ifPresent(defense -> {
			for (JuryMember member : defense.getMembers()) {
				if (member.getTeacher() != null) {
					juryMembers.add(new JuryMemberResponse(
							member.getTeacher().getFirstName() + " " + member.getTeacher().getLastName(),
							member.getRoleName()));
				}
			}
		});

		String date = null;
		String startTime = null;
		String roomName = null;
		String status = "pending";

		if (defenseOpt.isPresent()) {
			Defense defense = defenseOpt.get();
			date = defense.getDate().toString();
			startTime = defense.getTime().toString();
			roomName = defense.getRoom() != null ? defense.getRoom().getName() : "";
			status = "scheduled";
		}

		return new StudentDefenseResponse(project.getTitle(), project.getDescription(),
				project.getSupervisor() != null
						? project.getSupervisor().getFirstName() + " " + project.getSupervisor().getLastName()
						: null,
				juryMembers, date, startTime, "", roomName, status, null, null);
	}
}
