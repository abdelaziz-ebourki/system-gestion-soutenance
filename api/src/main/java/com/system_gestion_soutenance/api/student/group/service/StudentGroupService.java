package com.system_gestion_soutenance.api.student.group.service;

import com.system_gestion_soutenance.api.admin.config.settings.defense.entity.DefenseSettings;
import com.system_gestion_soutenance.api.admin.config.settings.defense.repository.DefenseSettingsRepository;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.common.mapper.StudentGroupMapper;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
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
	private final DefenseSettingsRepository defenseSettingsRepository;
	private final DefenseSessionRepository defenseSessionRepository;
	private final StudentGroupMapper studentGroupMapper;
	private final ApplicationEventPublisher eventPublisher;
	private final SecurityService securityService;

	public StudentGroupService(GroupRepository groupRepository, StudentRepository studentRepository,
			DefenseSettingsRepository defenseSettingsRepository, DefenseSessionRepository defenseSessionRepository,
			StudentGroupMapper studentGroupMapper, ApplicationEventPublisher eventPublisher,
			SecurityService securityService) {
		this.groupRepository = groupRepository;
		this.studentRepository = studentRepository;
		this.defenseSettingsRepository = defenseSettingsRepository;
		this.defenseSessionRepository = defenseSessionRepository;
		this.studentGroupMapper = studentGroupMapper;
		this.eventPublisher = eventPublisher;
		this.securityService = securityService;
	}

	@Transactional(readOnly = true)
	public StudentGroupWorkspaceResponse getWorkspace(Long studentId) {
		Group currentGroup = groupRepository.findByStudentId(studentId).orElse(null);

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

		DefenseSettings ds = defenseSettingsRepository.findById(1L).orElse(null);
		String startDate = ds != null ? ds.getGroupCreationStartDate() : "";
		String endDate = ds != null ? ds.getGroupCreationEndDate() : "";

		return new StudentGroupWorkspaceResponse(currentDetails, available, startDate, endDate,
				isCreationOpen(startDate, endDate));
	}

	@Transactional
	public Group createGroup(Long studentId) {
		if (groupRepository.findByStudentId(studentId).isPresent()) {
			throw new InvalidBusinessStateException("Vous êtes déjà membre d'un groupe");
		}
		if (!isCreationPeriodOpen()) {
			throw new InvalidBusinessStateException("La période de création de groupes est fermée");
		}

		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new InvalidBusinessStateException("Étudiant introuvable"));

		DefenseSession activeSession = resolveActiveSession();

		Group group = new Group();
		group.setGroupName(String.format("Groupe_%d", groupRepository.count() + 1));
		group.setStudents(new ArrayList<>(List.of(student)));
		group.setLeaderId(studentId);
		group.setSessionId(activeSession != null ? activeSession.getId() : null);
		return groupRepository.save(group);
	}

	@Transactional
	public Group joinGroup(Long groupId, Long studentId) {
		if (groupRepository.findByStudentId(studentId).isPresent()) {
			throw new InvalidBusinessStateException("Vous êtes déjà membre d'un groupe");
		}
		if (!isCreationPeriodOpen()) {
			throw new InvalidBusinessStateException("La période de création de groupes est fermée");
		}

		Group group = groupRepository.findById(groupId)
				.orElseThrow(() -> new EntityNotFoundException("Groupe non trouvé"));

		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new InvalidBusinessStateException("Étudiant introuvable"));

		if (group.getStudents() == null) {
			group.setStudents(new ArrayList<>());
		}
		if (group.getStudents().stream().anyMatch(s -> s.getId().equals(studentId))) {
			throw new InvalidBusinessStateException("Vous êtes déjà dans ce groupe");
		}
		int maxSize = resolveMaxGroupSize(group.getSessionId());
		if (maxSize > 0 && group.getStudents().size() >= maxSize) {
			throw new InvalidBusinessStateException("Le groupe a atteint sa taille maximale");
		}
		group.getStudents().add(student);
		return groupRepository.save(group);
	}

	private DefenseSession resolveActiveSession() {
		return defenseSessionRepository.findActiveSession(LocalDate.now()).orElse(null);
	}

	private int resolveMaxGroupSize(Long sessionId) {
		if (sessionId != null) {
			DefenseSession ds = defenseSessionRepository.findById(sessionId).orElse(null);
			if (ds != null && ds.getMaxGroupSize() > 0)
				return ds.getMaxGroupSize();
		}
		return 0;
	}

	private boolean isCreationPeriodOpen() {
		DefenseSettings ds = defenseSettingsRepository.findById(1L).orElse(null);
		if (ds == null)
			return false;
		return isCreationOpen(ds.getGroupCreationStartDate(), ds.getGroupCreationEndDate());
	}

	@Transactional
	public void leaveGroup(Long studentId) {
		Group group = groupRepository.findByStudentId(studentId)
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

	private boolean isCreationOpen(String startDate, String endDate) {
		try {
			LocalDate now = LocalDate.now();
			LocalDate start = LocalDate.parse(startDate);
			LocalDate end = LocalDate.parse(endDate);
			return !now.isBefore(start) && !now.isAfter(end);
		} catch (Exception e) {
			return false;
		}
	}
}