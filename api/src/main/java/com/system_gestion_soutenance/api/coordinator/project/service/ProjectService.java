package com.system_gestion_soutenance.api.coordinator.project.service;

import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.project.dto.CreateProjectRequest;
import com.system_gestion_soutenance.api.coordinator.project.dto.ProjectResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
	public List<ProjectResponse> findAll() {
		List<Project> projects = projectRepository.findAllWithDetails();
		Map<Long, Long> projectGroupIds = buildProjectGroupIdMap(projects);
		return projects.stream().map(p -> toResponse(p, projectGroupIds)).collect(Collectors.toList());
	}

	private Map<Long, Long> buildProjectGroupIdMap(List<Project> projects) {
		List<Long> projectIds = projects.stream().map(Project::getId).toList();
		return groupRepository.findByProjectIdIn(projectIds).stream().filter(g -> g.getProject() != null)
				.collect(Collectors.toMap(g -> g.getProject().getId(), g -> g.getId(), (a, b) -> a));
	}

	@Transactional
	public ProjectResponse create(CreateProjectRequest request) {
		Teacher supervisor = teacherRepository.findById(request.supervisorId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Encadrant introuvable"));

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

		return toResponse(projectRepository.save(project));
	}

	@Transactional
	public ProjectResponse update(Long id, UpdateProjectRequest updates) {
		Project project = projectRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projet non trouvé"));

		if (updates.title() != null)
			project.setTitle(updates.title());
		if (updates.description() != null)
			project.setDescription(updates.description());
		if (updates.defenseType() != null)
			project.setDefenseType(updates.defenseType());

		return toResponse(projectRepository.save(project));
	}

	@Transactional
	public void delete(Long id) {
		Project project = projectRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projet non trouvé"));

		if (!juryRepository.findByProjectId(id).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Impossible de supprimer ce projet car des jurys y sont rattachés");
		}
		if (!groupRepository.findByProjectId(id).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Impossible de supprimer ce projet car des groupes y sont rattachés");
		}
		if (slotAssignmentRepository.existsByProjectId(id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Impossible de supprimer ce projet car des soutenances sont planifiées");
		}

		projectRepository.delete(project);
	}

	private ProjectResponse toResponse(Project project, Map<Long, Long> projectGroupIds) {
		return buildResponse(project, projectGroupIds.get(project.getId()));
	}

	private ProjectResponse toResponse(Project project) {
		Long groupId = null;
		var groups = groupRepository.findByProjectId(project.getId());
		if (!groups.isEmpty()) {
			groupId = groups.get(0).getId();
		}
		return buildResponse(project, groupId);
	}

	private ProjectResponse buildResponse(Project project, Long groupId) {
		List<String> studentNames = project.getStudents() != null
				? project.getStudents().stream().map(s -> s.getFirstName() + " " + s.getLastName())
						.collect(Collectors.toList())
				: List.of();

		return new ProjectResponse(project.getId(), project.getTitle(), project.getDescription(),
				project.getDefenseType(), groupId,
				project.getSupervisor() != null
						? project.getSupervisor().getFirstName() + " " + project.getSupervisor().getLastName()
						: null,
				studentNames);
	}

}
