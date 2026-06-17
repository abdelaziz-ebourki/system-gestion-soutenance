package com.system_gestion_soutenance.api.coordinator.project.service;

import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.dto.BulkImportResult;
import com.system_gestion_soutenance.api.coordinator.project.dto.BulkProjectEntry;
import com.system_gestion_soutenance.api.coordinator.project.dto.BulkProjectRequest;
import com.system_gestion_soutenance.api.coordinator.project.dto.BulkProjectResponse;
import com.system_gestion_soutenance.api.coordinator.project.dto.CreateProjectRequest;
import com.system_gestion_soutenance.api.coordinator.project.dto.UpdateProjectRequest;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.ProjectStatus;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.notification.event.ProjectProposedEvent;
import com.system_gestion_soutenance.api.notification.event.ProjectStatusChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import java.util.*;
import java.util.stream.Collectors;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import com.system_gestion_soutenance.api.common.exception.ResourceConflictException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@SuppressWarnings("PMD")

@Service
public class ProjectService {

	private final ProjectRepository projectRepository;
	private final TeacherRepository teacherRepository;
	private final GroupRepository groupRepository;
	private final DefenseRepository defenseRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final SecurityService securityService;
	private final UserRepository userRepository;

	public ProjectService(ProjectRepository projectRepository, TeacherRepository teacherRepository,
			GroupRepository groupRepository, DefenseRepository defenseRepository,
			ApplicationEventPublisher eventPublisher, SecurityService securityService, UserRepository userRepository) {
		this.projectRepository = projectRepository;
		this.teacherRepository = teacherRepository;
		this.groupRepository = groupRepository;
		this.defenseRepository = defenseRepository;
		this.eventPublisher = eventPublisher;
		this.securityService = securityService;
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public List<Project> findAll() {
		return projectRepository.findAllWithDetails();
	}

	public PaginatedResponse<Project> findAll(int page, int limit) {
		Page<Project> projectPage = projectRepository.findAllWithDetails(PageRequest.of(page, limit));
		return new PaginatedResponse<>(projectPage.getContent(), projectPage.getTotalElements(),
				projectPage.getTotalPages(), page, limit);
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

		Project project = new Project();
		project.setTitle(request.title());
		project.setDescription(request.description());
		project.setDefenseType(request.defenseType());
		project.setStatus(ProjectStatus.PENDING);
		project.setSupervisor(supervisor);

		Project saved = projectRepository.save(project);
		eventPublisher.publishEvent(new ProjectProposedEvent(securityService.getCurrentUserEmail(), saved.getId(),
				saved.getTitle(), "Divers"));
		return saved;
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

	@Audited(action = "UPDATE_STATUS", entity = "Project")
	@Transactional
	public Project updateStatus(Long id, ProjectStatus newStatus) {
		Project project = projectRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Projet non trouvé"));

		ProjectStatus current = project.getStatus();
		if (current == newStatus) {
			throw new InvalidBusinessStateException("Le projet est déjà à l'état " + newStatus.name());
		}
		if (current == ProjectStatus.PENDING && newStatus != ProjectStatus.APPROVED
				&& newStatus != ProjectStatus.REJECTED) {
			throw new InvalidBusinessStateException("Un projet en attente ne peut être approuvé ou rejeté uniquement");
		}
		if (current == ProjectStatus.APPROVED && newStatus != ProjectStatus.PENDING) {
			throw new InvalidBusinessStateException("Un projet approuvé ne peut revenir qu'à l'état en attente");
		}
		if (current == ProjectStatus.REJECTED && newStatus != ProjectStatus.PENDING) {
			throw new InvalidBusinessStateException("Un projet rejeté ne peut revenir qu'à l'état en attente");
		}

		project.setStatus(newStatus);
		Project saved = projectRepository.save(project);
		eventPublisher.publishEvent(new ProjectStatusChangedEvent(securityService.getCurrentUserEmail(), saved.getId(),
				saved.getTitle(), current.name(), newStatus.name()));
		return saved;
	}

	@Audited(action = "DELETE", entity = "Project")
	@Transactional
	public void delete(Long id) {
		Project project = projectRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Projet non trouvé"));

		if (defenseRepository.findByProject(project).isPresent()) {
			throw new ResourceConflictException(
					"Impossible de supprimer ce projet car une soutenance lui est rattachée");
		}
		if (!groupRepository.findByProjectId(id).isEmpty()) {
			throw new ResourceConflictException("Impossible de supprimer ce projet car des groupes y sont rattachés");
		}

		projectRepository.delete(project);
	}

	@Transactional
	public BulkImportResult bulkImport(BulkProjectRequest request) {
		List<BulkImportResult.BulkImportError> errors = new ArrayList<>();
		List<BulkProjectResponse> created = new ArrayList<>();

		int line = 0;
		for (BulkProjectEntry entry : request.projects()) {
			line++;
			try {
				Teacher supervisor = resolveSupervisor(entry);
				if (supervisor == null) {
					String detail = entry.supervisorEmail() != null
							? "Encadrant introuvable avec l'email " + entry.supervisorEmail()
							: "Encadrant introuvable avec l'id " + entry.supervisorId();
					errors.add(new BulkImportResult.BulkImportError(line, detail));
					continue;
				}

				Project project = new Project();
				project.setTitle(entry.title());
				project.setDescription(entry.description());
				project.setDefenseType(entry.defenseType());
				project.setStatus(ProjectStatus.PENDING);
				project.setSupervisor(supervisor);

				Project saved = projectRepository.save(project);
				created.add(new BulkProjectResponse(saved.getId(), saved.getTitle()));
			} catch (Exception e) {
				errors.add(new BulkImportResult.BulkImportError(line, e.getMessage()));
			}
		}

		return new BulkImportResult(request.projects().size(), created.size(), created, errors);
	}

	private Teacher resolveSupervisor(BulkProjectEntry entry) {
		if (entry.supervisorId() != null) {
			return teacherRepository.findById(entry.supervisorId()).orElse(null);
		}
		if (entry.supervisorEmail() != null) {
			User user = userRepository.findByEmail(entry.supervisorEmail()).orElse(null);
			if (user instanceof Teacher teacher) {
				return teacher;
			}
		}
		return null;
	}

}