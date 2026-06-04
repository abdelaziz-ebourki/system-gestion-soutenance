package com.system_gestion_soutenance.api.coordinator.group.service;

import com.system_gestion_soutenance.api.coordinator.group.dto.CreateGroupRequest;
import com.system_gestion_soutenance.api.coordinator.group.dto.GroupResponse;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GroupService {

	private final GroupRepository groupRepository;
	private final ProjectRepository projectRepository;
	private final StudentRepository studentRepository;

	public GroupService(GroupRepository groupRepository, ProjectRepository projectRepository,
			StudentRepository studentRepository) {
		this.groupRepository = groupRepository;
		this.projectRepository = projectRepository;
		this.studentRepository = studentRepository;
	}

	@Transactional(readOnly = true)
	public List<GroupResponse> findAll() {
		return groupRepository.findAllWithDetails().stream().map(this::toResponse).collect(Collectors.toList());
	}

	@Transactional
	public GroupResponse create(CreateGroupRequest request) {
		Project project = projectRepository.findById(request.projectId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Projet introuvable"));

		List<Student> students = Collections.emptyList();
		if (request.studentIds() != null) {
			students = studentRepository.findAllById(request.studentIds());
		}

		Group group = new Group();
		group.setGroupName(request.groupName());
		group.setProject(project);
		group.setStudents(students);
		group.setSessionId(request.sessionId());

		return toResponse(groupRepository.save(group));
	}

	@Transactional
	public void delete(Long id) {
		if (!groupRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Groupe non trouvé");
		}
		groupRepository.deleteById(id);
	}

	private GroupResponse toResponse(Group group) {
		List<String> studentNames = group.getStudents() != null
				? group.getStudents().stream().map(s -> s.getFirstName() + " " + s.getLastName())
						.collect(Collectors.toList())
				: List.of();

		return new GroupResponse(group.getId(), group.getGroupName(),
				group.getProject() != null ? group.getProject().getId() : null,
				group.getStudents() != null ? group.getStudents().size() : 0, studentNames);
	}
}
