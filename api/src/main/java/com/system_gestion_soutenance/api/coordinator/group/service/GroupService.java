package com.system_gestion_soutenance.api.coordinator.group.service;

import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.coordinator.group.dto.CreateGroupRequest;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import java.util.Collections;
import java.util.List;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	public List<Group> findAll() {
		return groupRepository.findAllWithDetails();
	}

	@Audited(action = "CREATE", entity = "Group")
	@Transactional
	public Group create(CreateGroupRequest request) {
		Project project = projectRepository.findById(request.projectId())
				.orElseThrow(() -> new InvalidBusinessStateException("Projet introuvable"));

		List<Student> students = Collections.emptyList();
		if (request.studentIds() != null) {
			students = studentRepository.findAllById(request.studentIds());
		}

		Group group = new Group();
		group.setGroupName(request.groupName());
		group.setProject(project);
		group.setStudents(students);
		group.setSessionId(request.sessionId());

		return groupRepository.save(group);
	}

	@Audited(action = "DELETE", entity = "Group")
	@Transactional
	public void delete(Long id) {
		if (!groupRepository.existsById(id)) {
			throw new EntityNotFoundException("Groupe non trouvé");
		}
		groupRepository.deleteById(id);
	}
}
