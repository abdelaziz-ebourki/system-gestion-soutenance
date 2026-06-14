package com.system_gestion_soutenance.api.coordinator.group.service;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.coordinator.group.dto.CreateGroupRequest;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.notification.event.StudentLeftGroupEvent;
import org.springframework.context.ApplicationEventPublisher;
import java.util.Collections;
import java.util.List;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@SuppressWarnings("PMD")

@Service
public class GroupService {

	private final GroupRepository groupRepository;
	private final ProjectRepository projectRepository;
	private final StudentRepository studentRepository;
	private final DefenseSessionRepository defenseSessionRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final SecurityService securityService;

	public GroupService(GroupRepository groupRepository, ProjectRepository projectRepository,
			StudentRepository studentRepository, DefenseSessionRepository defenseSessionRepository,
			ApplicationEventPublisher eventPublisher, SecurityService securityService) {
		this.groupRepository = groupRepository;
		this.projectRepository = projectRepository;
		this.studentRepository = studentRepository;
		this.defenseSessionRepository = defenseSessionRepository;
		this.eventPublisher = eventPublisher;
		this.securityService = securityService;
	}

	@Transactional(readOnly = true)
	public List<Group> findAll() {
		return groupRepository.findAllWithDetails();
	}

	public PaginatedResponse<Group> findAll(int page, int limit) {
		Page<Group> groupPage = groupRepository.findAllWithDetails(PageRequest.of(page, limit));
		return new PaginatedResponse<>(groupPage.getContent(), groupPage.getTotalElements(), groupPage.getTotalPages(),
				page, limit);
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

		if (request.sessionId() != null) {
			DefenseSession ds = defenseSessionRepository.findById(request.sessionId()).orElse(null);
			if (ds != null && ds.getMaxGroupSize() > 0 && students.size() > ds.getMaxGroupSize()) {
				throw new InvalidBusinessStateException("Le groupe a atteint sa taille maximale");
			}
		}

		Long requestedLeaderId = request.leaderId();
		Long leaderId;
		if (requestedLeaderId != null) {
			leaderId = requestedLeaderId;
		} else if (!students.isEmpty()) {
			leaderId = students.get(0).getId();
		} else {
			leaderId = null;
		}
		if (leaderId != null && !students.isEmpty() && students.stream().noneMatch(s -> s.getId().equals(leaderId))) {
			throw new InvalidBusinessStateException("Le leader doit être membre du groupe");
		}

		Group group = new Group();
		group.setGroupName(request.groupName());
		group.setProject(project);
		group.setStudents(students);
		group.setSessionId(request.sessionId());
		group.setLeaderId(leaderId);

		return groupRepository.save(group);
	}

	@Audited(action = "REMOVE_MEMBER", entity = "Group")
	@Transactional
	public void removeMember(Long groupId, Long studentId) {
		Group group = groupRepository.findById(groupId)
				.orElseThrow(() -> new EntityNotFoundException("Groupe non trouvé"));

		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new EntityNotFoundException("Étudiant introuvable"));
		String studentName = student.getFirstName() + " " + student.getLastName();

		boolean removed = group.getStudents().removeIf(s -> s.getId().equals(studentId));
		if (!removed) {
			throw new InvalidBusinessStateException("L'étudiant n'est pas membre de ce groupe");
		}

		if (group.getStudents().isEmpty()) {
			if (group.getProject() != null) {
				throw new InvalidBusinessStateException("Impossible de supprimer un groupe ayant un projet assigné");
			}
			groupRepository.deleteById(group.getId());
			eventPublisher.publishEvent(
					new StudentLeftGroupEvent(securityService.getCurrentUserEmail(), studentId, studentName, groupId));
			return;
		}

		if (group.getLeaderId() != null && group.getLeaderId().equals(studentId)) {
			group.setLeaderId(group.getStudents().get(0).getId());
		}

		groupRepository.save(group);
		eventPublisher.publishEvent(
				new StudentLeftGroupEvent(securityService.getCurrentUserEmail(), studentId, studentName, groupId));
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