package com.system_gestion_soutenance.api.coordinator.group.service;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.coordinator.group.document.GroupDocumentService;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.coordinator.group.dto.CreateGroupRequest;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.entity.GroupStatus;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.notification.event.StudentLeftGroupEvent;
import org.springframework.context.ApplicationEventPublisher;
import java.time.LocalDate;
import java.util.ArrayList;
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
	private final GroupDocumentService groupDocumentService;

	public GroupService(GroupRepository groupRepository, ProjectRepository projectRepository,
			StudentRepository studentRepository, DefenseSessionRepository defenseSessionRepository,
			ApplicationEventPublisher eventPublisher, SecurityService securityService,
			GroupDocumentService groupDocumentService) {
		this.groupRepository = groupRepository;
		this.projectRepository = projectRepository;
		this.studentRepository = studentRepository;
		this.defenseSessionRepository = defenseSessionRepository;
		this.eventPublisher = eventPublisher;
		this.securityService = securityService;
		this.groupDocumentService = groupDocumentService;
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

		DefenseSession defenseSession = null;
		if (request.sessionId() != null) {
			defenseSession = defenseSessionRepository.findById(request.sessionId()).orElse(null);
			if (defenseSession != null && defenseSession.getMaxGroupSize() > 0
					&& students.size() > defenseSession.getMaxGroupSize()) {
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
		group.setDefenseSession(defenseSession);
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

	@Audited(action = "UPDATE_PROJECT", entity = "Group")
	@Transactional
	public Group updateProject(Long id, Long projectId) {
		Group group = groupRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Groupe non trouvé"));
		Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new InvalidBusinessStateException("Projet introuvable"));
		group.setProject(project);
		Group saved = groupRepository.save(group);

		List<com.system_gestion_soutenance.api.coordinator.group.document.GroupDocument> existing = groupDocumentService
				.findByGroup(id);
		if (existing.isEmpty()) {
			LocalDate deadline = group.getDefenseSession() != null
					? LocalDate.parse(group.getDefenseSession().getGroupCreationEndDate())
					: null;
			groupDocumentService.createDefaultDocuments(id, deadline);
		}

		return saved;
	}

	@Audited(action = "DELETE", entity = "Group")
	@Transactional
	public void delete(Long id) {
		if (!groupRepository.existsById(id)) {
			throw new EntityNotFoundException("Groupe non trouvé");
		}
		groupRepository.deleteById(id);
	}

	@Audited(action = "APPROVE", entity = "Group")
	@Transactional
	public Group approveGroup(Long groupId) {
		Group group = groupRepository.findById(groupId)
				.orElseThrow(() -> new EntityNotFoundException("Groupe non trouvé"));
		if (group.getStatus() != GroupStatus.PENDING) {
			throw new InvalidBusinessStateException("Seuls les groupes en attente peuvent être approuvés");
		}
		group.setStatus(GroupStatus.ACTIVE);
		return groupRepository.save(group);
	}

	@Audited(action = "REJECT", entity = "Group")
	@Transactional
	public void rejectGroup(Long groupId) {
		Group group = groupRepository.findById(groupId)
				.orElseThrow(() -> new EntityNotFoundException("Groupe non trouvé"));
		if (group.getStatus() != GroupStatus.PENDING) {
			throw new InvalidBusinessStateException("Seuls les groupes en attente peuvent être rejetés");
		}
		group.getStudents().clear();
		groupRepository.delete(group);
	}

	@Audited(action = "EXTEND_GROUP_FORMATION", entity = "DefenseSession")
	@Transactional
	public DefenseSession extendGroupFormation(Long sessionId, int days) {
		DefenseSession session = defenseSessionRepository.findById(sessionId)
				.orElseThrow(() -> new EntityNotFoundException("Session introuvable"));
		if (session.getGroupFormationEndDate() == null) {
			throw new InvalidBusinessStateException("Cette session n'a pas de date de fin de formation");
		}
		session.setGroupFormationEndDate(session.getGroupFormationEndDate().plusDays(days));
		return defenseSessionRepository.save(session);
	}

	@Transactional
	public Group assignStudentToGroup(Long studentId, Long groupId) {
		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new EntityNotFoundException("Étudiant introuvable"));
		Group group = groupRepository.findById(groupId)
				.orElseThrow(() -> new EntityNotFoundException("Groupe non trouvé"));

		if (groupRepository.findFirstByStudentsIdOrderByIdAsc(studentId).isPresent()) {
			throw new InvalidBusinessStateException("Cet étudiant est déjà dans un groupe");
		}
		if (group.getStudents() == null) {
			group.setStudents(new ArrayList<>());
		}
		if (group.getStudents().stream().anyMatch(s -> s.getId().equals(studentId))) {
			throw new InvalidBusinessStateException("L'étudiant est déjà dans ce groupe");
		}
		DefenseSession session = group.getDefenseSession();
		int maxSize = session != null && session.getMaxGroupSize() > 0 ? session.getMaxGroupSize() : 0;
		if (maxSize > 0 && group.getStudents().size() >= maxSize) {
			throw new InvalidBusinessStateException("Le groupe a atteint sa taille maximale");
		}
		group.getStudents().add(student);
		return groupRepository.save(group);
	}

	@Transactional(readOnly = true)
	public List<Student> getUngroupedStudents(Long sessionId) {
		List<Group> sessionGroups = groupRepository.findByDefenseSessionId(sessionId);
		List<Student> allStudents = studentRepository.findAll();
		List<Long> groupedStudentIds = sessionGroups.stream().filter(g -> g.getStatus() == GroupStatus.ACTIVE)
				.flatMap(g -> g.getStudents() != null
						? g.getStudents().stream().map(Student::getId)
						: java.util.stream.Stream.empty())
				.toList();
		return allStudents.stream().filter(s -> !groupedStudentIds.contains(s.getId())).toList();
	}
}