package com.system_gestion_soutenance.api.teacher.schedule.service;

import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.defense.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.teacher.schedule.dto.TeacherScheduleResponse;
import com.system_gestion_soutenance.api.teacher.schedule.dto.SlotDetails;
import com.system_gestion_soutenance.api.user.entity.Student;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class TeacherScheduleService {

	private final DefenseRepository defenseRepository;
	private final ProjectRepository projectRepository;
	private final GroupRepository groupRepository;

	public TeacherScheduleService(DefenseRepository defenseRepository, ProjectRepository projectRepository,
			GroupRepository groupRepository) {
		this.defenseRepository = defenseRepository;
		this.projectRepository = projectRepository;
		this.groupRepository = groupRepository;
	}

	public TeacherScheduleResponse getSchedule(Long teacherId) {
		Set<Long> projectIdsForTeacher = new HashSet<>();
		Map<Long, String> projectRoles = new HashMap<>();

		List<Defense> allDefenses = defenseRepository.findAllWithMembers();

		for (Defense defense : allDefenses) {
			if (defense.getProject() == null) {
				continue;
			}
			for (JuryMember member : defense.getMembers()) {
				if (member.getTeacher() != null && member.getTeacher().getId().equals(teacherId)) {
					Long pid = defense.getProject().getId();
					projectIdsForTeacher.add(pid);
					projectRoles.put(pid, member.getRoleName());
				}
			}
		}

		List<Project> allProjects = projectRepository.findAll();
		for (Project project : allProjects) {
			if (project.getSupervisor() != null && project.getSupervisor().getId().equals(teacherId)) {
				Long pid = project.getId();
				projectIdsForTeacher.add(pid);
				projectRoles.putIfAbsent(pid, "supervisor");
			}
		}

		Map<Long, List<String>> projectStudents = new HashMap<>();
		for (Group group : groupRepository.findAll()) {
			Long pid = group.getProject().getId();
			if (projectIdsForTeacher.contains(pid)) {
				projectStudents.put(pid, extractStudentNames(group.getStudents()));
			}
		}
		for (Project project : allProjects) {
			Long pid = project.getId();
			if (projectIdsForTeacher.contains(pid) && !projectStudents.containsKey(pid)) {
				projectStudents.put(pid, extractStudentNames(project.getStudents()));
			}
		}

		List<SlotDetails> result = new ArrayList<>();
		for (Defense defense : allDefenses) {
			if (defense.getProject() == null) {
				continue;
			}
			Long pid = defense.getProject().getId();
			if (!projectIdsForTeacher.contains(pid))
				continue;

			result.add(new SlotDetails(defense.getId(), pid, defense.getProject().getTitle(),
					projectStudents.getOrDefault(pid, List.of()), defense.getDate().toString(),
					defense.getTime().toString(), "", defense.getRoom() != null ? defense.getRoom().getName() : "",
					projectRoles.getOrDefault(pid, ""), "scheduled"));
		}

		return new TeacherScheduleResponse(result);
	}

	private List<String> extractStudentNames(List<Student> students) {
		if (students == null) {
			return List.of();
		}
		return students.stream().map(s -> s.getFirstName() + " " + s.getLastName()).collect(Collectors.toList());
	}
}
