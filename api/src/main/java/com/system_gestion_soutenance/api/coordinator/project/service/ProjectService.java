package com.system_gestion_soutenance.api.coordinator.project.service;

import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.project.dto.CreateProjectRequest;
import com.system_gestion_soutenance.api.coordinator.project.dto.UpdateProjectRequest;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.ProjectStatus;
import com.system_gestion_soutenance.api.coordinator.schedule.repository.SlotAssignmentRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import java.util.*;
import java.util.stream.Collectors;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import com.system_gestion_soutenance.api.common.exception.ResourceConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

	private final ProjectRepository projectRepository;
	private final TeacherRepository teacherRepository;
	private final StudentRepository studentRepository;
	private final GroupRepository groupRepository;
	private final JuryRepository juryRepository;
	private final SlotAssignmentRepository slotAssignmentRepository;

	public ProjectService(ProjectRepository projectRepository, TeacherRepository teacherRepository,
			StudentRepository studentRepository, GroupRepository groupRepository, JuryRepository juryRepository,
			SlotAssignmentRepository slotAssignmentRepository) {
		this.projectRepository = projectRepository;
		this.teacherRepository = teacherRepository;
		this.studentRepository = studentRepository;
		this.groupRepository = groupRepository;
		this.juryRepository = juryRepository;
		this.slotAssignmentRepository = slotAssignmentRepository;
	}

	@Transactional(readOnly = true)
	public List<Project> findAll() {
		return projectRepository.findAllWithDetails();
	}

	public Map<Long, Long> buildProjectGroupIdMap(List<Project> projects) {
		List<Long> projectIds = projects.stream().map(Project::getId).toList();
		if (projectIds.isEmpty())
			return Map.of();
		return groupRepository.findByProjectIdIn(projectIds).stream().filter(g -> g.getProject() != null)
				.collect(Collectors.toMap(g -> g.getProject().getId(), g -> g.getId(), (a, b) -> a));
	}

	@Audited(action = "CREATE", entity = "Project")
	@Transactional
	public Project create(CreateProjectRequest request) {
		Teacher supervisor = teacherRepository.findById(request.supervisorId())
				.orElseThrow(() -> new InvalidBusinessStateException("Encadrant introuvable"));

		List<Student> students = Collections.emptyList();
		if (request.studentIds() != null) {
			students = studentRepository.findAllById(request.studentIds());
		}

		Project project = new Project();
		project.setTitle(request.title());
		project.setDescription(request.description());
		project.setDefenseType(request.defenseType());
		project.setStatus(ProjectStatus.PENDING);
		project.setSupervisor(supervisor);
		project.setStudents(students);

		return projectRepository.save(project);
	}

	@Audited(action = "UPDATE", entity = "Project")
	@Transactional
	public Project update(Long id, UpdateProjectRequest updates) {
		Project project = projectRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Projet non trouvé"));

		if (updates.title() != null)
			project.setTitle(updates.title());
		if (updates.description() != null)
			project.setDescription(updates.description());
		if (updates.defenseType() != null)
			project.setDefenseType(updates.defenseType());

		return projectRepository.save(project);
	}

	@Audited(action = "DELETE", entity = "Project")
	@Transactional
	public void delete(Long id) {
		Project project = projectRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Projet non trouvé"));

		if (!juryRepository.findByProjectId(id).isEmpty()) {
			throw new ResourceConflictException("Impossible de supprimer ce projet car des jurys y sont rattachés");
		}
		if (!groupRepository.findByProjectId(id).isEmpty()) {
			throw new ResourceConflictException("Impossible de supprimer ce projet car des groupes y sont rattachés");
		}
		if (slotAssignmentRepository.existsByProjectId(id)) {
			throw new ResourceConflictException(
					"Impossible de supprimer ce projet car des soutenances sont planifiées");
		}

		projectRepository.delete(project);
	}

}
