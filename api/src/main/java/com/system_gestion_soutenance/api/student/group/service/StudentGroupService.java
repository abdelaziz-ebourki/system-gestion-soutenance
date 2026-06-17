package com.system_gestion_soutenance.api.student.group.service;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.common.mapper.StudentGroupMapper;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.entity.GroupStatus;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import com.system_gestion_soutenance.api.student.group.dto.AvailableGroupResponse;
import com.system_gestion_soutenance.api.student.group.dto.StudentGroupWorkspaceResponse;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.notification.event.StudentLeftGroupEvent;
import org.springframework.context.ApplicationEventPublisher;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@SuppressWarnings("PMD")

@Service
public class StudentGroupService {

	private final GroupRepository groupRepository;
	private final StudentRepository studentRepository;
	private final DefenseSessionRepository defenseSessionRepository;
	private final ProjectRepository projectRepository;
	private final StudentGroupMapper studentGroupMapper;
	private final ApplicationEventPublisher eventPublisher;
	private final SecurityService securityService;

	public StudentGroupService(GroupRepository groupRepository, StudentRepository studentRepository,
			DefenseSessionRepository defenseSessionRepository, ProjectRepository projectRepository,
			StudentGroupMapper studentGroupMapper, ApplicationEventPublisher eventPublisher,
			SecurityService securityService) {
		this.groupRepository = groupRepository;
		this.studentRepository = studentRepository;
		this.defenseSessionRepository = defenseSessionRepository;
		this.projectRepository = projectRepository;
		this.studentGroupMapper = studentGroupMapper;
		this.eventPublisher = eventPublisher;
		this.securityService = securityService;
	}

	@Transactional(readOnly = true)
	public StudentGroupWorkspaceResponse getWorkspace(Long studentId) {
		Group currentGroup = groupRepository.findFirstByStudentsIdOrderByIdAsc(studentId).orElse(null);

		com.system_gestion_soutenance.api.student.group.dto.GroupDetailsResponse currentDetails = currentGroup != null
				? studentGroupMapper.toDetails(currentGroup, studentId)
				: null;

		List<AvailableGroupResponse> available = new ArrayList<>();
		for (Group g : groupRepository.findAllWithDetails()) {
			if (currentGroup == null || !g.getId().equals(currentGroup.getId())) {
				available.add(new AvailableGroupResponse(g.getId(), g.getGroupName(),
						g.getStudents() != null ? g.getStudents().size() : 0));
			}
		}

		DefenseSession activeSession = resolveActiveSession();
		String startDate = activeSession != null && activeSession.getGroupFormationStartDate() != null
				? activeSession.getGroupFormationStartDate().toString()
				: "";
		String endDate = activeSession != null && activeSession.getGroupFormationEndDate() != null
				? activeSession.getGroupFormationEndDate().toString()
				: "";

		return new StudentGroupWorkspaceResponse(currentDetails, available, startDate, endDate,
				isCreationOpen(startDate, endDate));
	}

	@Transactional
	public Group createGroup(Long studentId, String groupName, Long sessionId) {
		if (groupRepository.findFirstByStudentsIdOrderByIdAsc(studentId).isPresent()) {
			throw new InvalidBusinessStateException("Vous êtes déjà membre d'un groupe");
		}

		DefenseSession session = defenseSessionRepository.findById(sessionId)
				.orElseThrow(() -> new EntityNotFoundException("Session introuvable"));

		if (!isWithinGroupFormationWindow(session)) {
			throw new InvalidBusinessStateException("La période de formation de groupes est fermée pour cette session");
		}

		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new InvalidBusinessStateException("Étudiant introuvable"));

		Group group = new Group();
		group.setGroupName(groupName);
		group.setStudents(new ArrayList<>(List.of(student)));
		group.setLeaderId(studentId);
		group.setDefenseSession(session);
		group.setStatus(GroupStatus.PENDING);
		return groupRepository.save(group);
	}

	@Transactional
	public Group joinGroup(Long groupId, Long studentId) {
		if (groupRepository.findFirstByStudentsIdOrderByIdAsc(studentId).isPresent()) {
			throw new InvalidBusinessStateException("Vous êtes déjà membre d'un groupe");
		}

		Group group = groupRepository.findById(groupId)
				.orElseThrow(() -> new EntityNotFoundException("Groupe non trouvé"));

		DefenseSession session = group.getDefenseSession();
		if (session != null && !isWithinGroupFormationWindow(session)) {
			throw new InvalidBusinessStateException("La période de formation de groupes est fermée pour cette session");
		}

		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new InvalidBusinessStateException("Étudiant introuvable"));

		if (group.getStudents() == null) {
			group.setStudents(new ArrayList<>());
		}
		if (group.getStudents().stream().anyMatch(s -> s.getId().equals(studentId))) {
			throw new InvalidBusinessStateException("Vous êtes déjà dans ce groupe");
		}
		int maxSize = resolveMaxGroupSize(session);
		if (maxSize > 0 && group.getStudents().size() >= maxSize) {
			throw new InvalidBusinessStateException("Le groupe a atteint sa taille maximale");
		}
		group.getStudents().add(student);
		return groupRepository.save(group);
	}

	private int resolveMaxGroupSize(DefenseSession session) {
		if (session != null && session.getMaxGroupSize() > 0)
			return session.getMaxGroupSize();
		return 0;
	}

	private boolean isWithinGroupFormationWindow(DefenseSession session) {
		if (session == null)
			return false;
		if (session.getGroupFormationStartDate() == null || session.getGroupFormationEndDate() == null)
			return false;
		LocalDate now = LocalDate.now();
		return !now.isBefore(session.getGroupFormationStartDate()) && !now.isAfter(session.getGroupFormationEndDate());
	}

	private DefenseSession resolveActiveSession() {
		List<DefenseSession> sessions = defenseSessionRepository.findAll();
		return sessions.stream().filter(s -> {
			if (s.getGroupFormationStartDate() == null || s.getGroupFormationEndDate() == null)
				return false;
			LocalDate now = LocalDate.now();
			return !now.isBefore(s.getGroupFormationStartDate()) && !now.isAfter(s.getGroupFormationEndDate());
		}).findFirst().orElse(null);
	}

	private boolean isCreationOpen(String startDate, String endDate) {
		if (startDate == null || startDate.isEmpty() || endDate == null || endDate.isEmpty())
			return false;
		LocalDate now = LocalDate.now();
		return !now.isBefore(LocalDate.parse(startDate)) && !now.isAfter(LocalDate.parse(endDate));
	}

	@Transactional
	public void leaveGroup(Long studentId) {
		Group group = groupRepository.findFirstByStudentsIdOrderByIdAsc(studentId)
				.orElseThrow(() -> new InvalidBusinessStateException("Vous n'êtes membre d'aucun groupe"));

		if (group.getProject() != null) {
			throw new InvalidBusinessStateException("Impossible de quitter un groupe ayant un projet assigné");
		}

		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new EntityNotFoundException("Étudiant introuvable"));
		String studentName = student.getFirstName() + " " + student.getLastName();

		group.getStudents().removeIf(s -> s.getId().equals(studentId));

		if (group.getStudents().isEmpty()) {
			groupRepository.deleteById(group.getId());
			eventPublisher.publishEvent(new StudentLeftGroupEvent(securityService.getCurrentUserEmail(), studentId,
					studentName, group.getId()));
			return;
		}

		if (group.getLeaderId() != null && group.getLeaderId().equals(studentId)) {
			group.setLeaderId(group.getStudents().get(0).getId());
		}

		groupRepository.save(group);
		eventPublisher.publishEvent(new StudentLeftGroupEvent(securityService.getCurrentUserEmail(), studentId,
				studentName, group.getId()));
	}

	@Transactional
	public Group selectProject(Long groupId, Long projectId, Long studentId) {
		Group group = groupRepository.findById(groupId)
				.orElseThrow(() -> new EntityNotFoundException("Groupe non trouvé"));

		if (!group.getLeaderId().equals(studentId)) {
			throw new InvalidBusinessStateException("Seul le chef de groupe peut sélectionner un projet");
		}

		if (group.getProject() != null) {
			throw new InvalidBusinessStateException("Le groupe a déjà un projet assigné");
		}

		Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new EntityNotFoundException("Projet non trouvé"));

		if (project.getStatus() != com.system_gestion_soutenance.api.coordinator.project.entity.ProjectStatus.APPROVED
				&& project
						.getStatus() != com.system_gestion_soutenance.api.coordinator.project.entity.ProjectStatus.PENDING) {
			throw new InvalidBusinessStateException("Ce projet n'est pas disponible pour sélection");
		}

		group.setProject(project);
		return groupRepository.save(group);
	}

	@Transactional
	public Group cancelProjectSelection(Long groupId, Long studentId) {
		Group group = groupRepository.findById(groupId)
				.orElseThrow(() -> new EntityNotFoundException("Groupe non trouvé"));

		if (!group.getLeaderId().equals(studentId)) {
			throw new InvalidBusinessStateException("Seul le chef de groupe peut annuler la sélection");
		}

		if (group.getProject() == null) {
			throw new InvalidBusinessStateException("Le groupe n'a pas de projet sélectionné");
		}

		group.setProject(null);
		return groupRepository.save(group);
	}
}
