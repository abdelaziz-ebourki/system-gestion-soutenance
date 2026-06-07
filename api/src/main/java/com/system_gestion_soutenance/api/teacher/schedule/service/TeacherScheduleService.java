package com.system_gestion_soutenance.api.teacher.schedule.service;

import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.jury.entity.Jury;
import com.system_gestion_soutenance.api.coordinator.jury.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.coordinator.schedule.entity.SlotAssignment;
import com.system_gestion_soutenance.api.coordinator.schedule.repository.SlotAssignmentRepository;
import com.system_gestion_soutenance.api.teacher.schedule.dto.TeacherScheduleResponse;
import com.system_gestion_soutenance.api.teacher.schedule.dto.SlotDetails;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class TeacherScheduleService {

	private final SlotAssignmentRepository slotAssignmentRepository;
	private final JuryRepository juryRepository;
	private final ProjectRepository projectRepository;
	private final GroupRepository groupRepository;

	public TeacherScheduleService(SlotAssignmentRepository slotAssignmentRepository, JuryRepository juryRepository,
			ProjectRepository projectRepository, GroupRepository groupRepository) {
		this.slotAssignmentRepository = slotAssignmentRepository;
		this.juryRepository = juryRepository;
		this.projectRepository = projectRepository;
		this.groupRepository = groupRepository;
	}

	public TeacherScheduleResponse getSchedule(Long teacherId) {
		Set<Long> projectIdsForTeacher = new HashSet<>();
		Map<Long, String> projectRoles = new HashMap<>();

		for (Jury jury : juryRepository.findAll()) {
			for (JuryMember member : jury.getMembers()) {
				if (member.getTeacher() != null && member.getTeacher().getId().equals(teacherId)) {
					Long pid = jury.getProject().getId();
					projectIdsForTeacher.add(pid);
					projectRoles.put(pid, member.getRoleName());
				}
			}
		}

		for (Project project : projectRepository.findAll()) {
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
				List<String> names = group.getStudents() != null
						? group.getStudents().stream().map(s -> s.getFirstName() + " " + s.getLastName())
								.collect(Collectors.toList())
						: List.of();
				projectStudents.put(pid, names);
			}
		}
		for (Project project : projectRepository.findAll()) {
			Long pid = project.getId();
			if (projectIdsForTeacher.contains(pid) && !projectStudents.containsKey(pid)) {
				List<String> names = project.getStudents() != null
						? project.getStudents().stream().map(s -> s.getFirstName() + " " + s.getLastName())
								.collect(Collectors.toList())
						: List.of();
				projectStudents.put(pid, names);
			}
		}

		List<SlotDetails> result = new ArrayList<>();
		for (SlotAssignment slot : slotAssignmentRepository.findAll()) {
			Long pid = slot.getProjectId();
			if (pid == null || !projectIdsForTeacher.contains(pid))
				continue;

			Project project = projectRepository.findById(pid).orElse(null);
			if (project == null)
				continue;

			result.add(
					new SlotDetails(slot.getId(), pid, project.getTitle(), projectStudents.getOrDefault(pid, List.of()),
							slot.getDate(), slot.getTime(), "", slot.getRoom() != null ? slot.getRoom().getName() : "",
							projectRoles.getOrDefault(pid, ""), "scheduled"));
		}

		return new TeacherScheduleResponse(result);
	}
}
