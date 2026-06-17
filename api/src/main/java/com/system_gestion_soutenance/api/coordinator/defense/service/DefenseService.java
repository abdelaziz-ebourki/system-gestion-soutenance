package com.system_gestion_soutenance.api.coordinator.defense.service;

import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSessionStatus;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.admin.room.entity.Room;
import com.system_gestion_soutenance.api.admin.room.repository.RoomRepository;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.defense.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.entity.ProjectStatus;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.coordinator.schedule.dto.ScheduleRequest;
import com.system_gestion_soutenance.api.coordinator.schedule.dto.ScheduleResponse;
import com.system_gestion_soutenance.api.coordinator.jury.dto.CreateJuryRequest;
import com.system_gestion_soutenance.api.coordinator.jury.dto.UpdateJuryRequest;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.notification.event.DefenseCancelledEvent;
import com.system_gestion_soutenance.api.notification.event.DefenseSessionPublishedEvent;
import org.springframework.context.ApplicationEventPublisher;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@SuppressWarnings("PMD")

@Service
public class DefenseService {

	private final DefenseRepository defenseRepository;
	private final RoomRepository roomRepository;
	private final DefenseSessionRepository defenseSessionRepository;
	private final ProjectRepository projectRepository;
	private final GroupRepository groupRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final SecurityService securityService;
	private final TeacherRepository teacherRepository;

	public DefenseService(DefenseRepository defenseRepository, RoomRepository roomRepository,
			DefenseSessionRepository defenseSessionRepository, ProjectRepository projectRepository,
			GroupRepository groupRepository, ApplicationEventPublisher eventPublisher, SecurityService securityService,
			TeacherRepository teacherRepository) {
		this.defenseRepository = defenseRepository;
		this.roomRepository = roomRepository;
		this.defenseSessionRepository = defenseSessionRepository;
		this.projectRepository = projectRepository;
		this.groupRepository = groupRepository;
		this.eventPublisher = eventPublisher;
		this.securityService = securityService;
		this.teacherRepository = teacherRepository;
	}

	@Transactional(readOnly = true)
	public List<Defense> getSchedule() {
		return defenseRepository.findAllWithMembers();
	}

	public PaginatedResponse<Defense> getSchedule(int page, int limit) {
		Page<Defense> defensePage = defenseRepository.findAllWithMembers(PageRequest.of(page, limit));
		return new PaginatedResponse<>(defensePage.getContent(), defensePage.getTotalElements(),
				defensePage.getTotalPages(), page, limit);
	}

	public Map<Long, Project> buildProjectMap(List<Defense> defenses) {
		List<Long> projectIds = defenses.stream().filter(d -> d.getProject() != null).map(d -> d.getProject().getId())
				.distinct().toList();
		return projectRepository.findAllById(projectIds).stream().collect(Collectors.toMap(Project::getId, p -> p));
	}

	public Map<Long, List<String>> buildStudentNamesMap(Map<Long, Project> projectMap) {
		if (projectMap.isEmpty())
			return Map.of();
		List<Long> projectIds = new ArrayList<>(projectMap.keySet());
		Map<Long, List<String>> namesMap = new HashMap<>();
		var groups = groupRepository.findByProjectIdIn(projectIds);
		Map<Long, List<Group>> groupsByProject = groups.stream().filter(g -> g.getProject() != null)
				.collect(Collectors.groupingBy(g -> g.getProject().getId()));
		for (var entry : projectMap.entrySet()) {
			Long pid = entry.getKey();
			Project project = entry.getValue();
			namesMap.put(pid, resolveStudentNames(project, groupsByProject.get(pid)));
		}
		return namesMap;
	}

	@Audited(action = "BULK_CREATE", entity = "Defense")
	@Transactional
	public List<Defense> saveSchedule(ScheduleRequest request) {
		defenseRepository.deleteAll();

		for (var slotReq : request.slots()) {
			Defense defense = new Defense();
			defense.setDate(LocalDate.parse(slotReq.date()));
			defense.setTime(LocalTime.parse(slotReq.time()));

			if (slotReq.projectId() != null) {
				Project project = projectRepository.findById(slotReq.projectId()).orElseThrow(
						() -> new InvalidBusinessStateException("Projet introuvable: " + slotReq.projectId()));
				defense.setProject(project);
			}

			if (slotReq.roomId() != null) {
				Room room = roomRepository.findById(slotReq.roomId())
						.orElseThrow(() -> new InvalidBusinessStateException("Salle introuvable: " + slotReq.roomId()));
				defense.setRoom(room);
			}

			defenseRepository.save(defense);
		}

		return getSchedule();
	}

	@Audited(action = "CREATE", entity = "Jury")
	@Transactional
	public Defense createJury(CreateJuryRequest request) {
		Project project = projectRepository.findById(request.projectId())
				.orElseThrow(() -> new InvalidBusinessStateException("Projet introuvable"));

		Defense defense = defenseRepository.findByProject(project).orElseThrow(
				() -> new InvalidBusinessStateException("Aucun créneau de soutenance assigné à ce projet"));

		validateSupervisorNotInJury(project, request.members());
		validateNoDuplicateTeachers(request.members());

		List<JuryMember> members = request.members().stream().map(m -> mapToJuryMember(m, defense)).toList();

		defense.setMembers(members);
		return defenseRepository.save(defense);
	}

	@Audited(action = "UPDATE", entity = "Jury")
	@Transactional
	public Defense updateJury(Long defenseId, UpdateJuryRequest updates) {
		Defense defense = defenseRepository.findById(defenseId)
				.orElseThrow(() -> new EntityNotFoundException("Soutenance non trouvée"));

		Project project = defense.getProject();
		if (updates.projectId() != null) {
			project = projectRepository.findById(updates.projectId())
					.orElseThrow(() -> new InvalidBusinessStateException("Projet introuvable"));
			defense.setProject(project);
		}

		if (updates.members() != null) {
			validateSupervisorNotInJury(project, updates.members());
			validateNoDuplicateTeachers(updates.members());
			List<JuryMember> members = updates.members().stream().map(m -> mapToJuryMember(m, defense)).toList();
			defense.setMembers(members);
		}

		return defenseRepository.save(defense);
	}

	private JuryMember mapToJuryMember(Object m, Defense defense) {
		if (m instanceof CreateJuryRequest.MemberEntry entry) {
			return createJuryMember(entry.teacherId(), entry.roleName(), entry.externalName(),
					entry.externalInstitution(), entry.externalEmail(), defense);
		} else if (m instanceof UpdateJuryRequest.MemberEntry entry) {
			return createJuryMember(entry.teacherId(), entry.roleName(), entry.externalName(),
					entry.externalInstitution(), entry.externalEmail(), defense);
		}
		throw new InvalidBusinessStateException("Type de membre invalide");
	}

	private JuryMember createJuryMember(Long teacherId, String roleName, String externalName,
			String externalInstitution, String externalEmail, Defense defense) {
		if (externalName != null && !externalName.isBlank()) {
			return new JuryMember(null, null, roleName, defense, externalName, externalInstitution, externalEmail);
		}
		var teacher = teacherRepository.findById(teacherId)
				.orElseThrow(() -> new InvalidBusinessStateException("Enseignant introuvable: " + teacherId));
		return new JuryMember(null, teacher, roleName, defense, null, null, null);
	}

	private void validateSupervisorNotInJury(Project project, List<?> members) {
		if (project == null || project.getSupervisor() == null) {
			return;
		}
		List<Group> groups = groupRepository.findByProjectId(project.getId());
		if (groups.isEmpty()) {
			return;
		}
		DefenseSession session = groups.get(0).getDefenseSession();
		if (session == null || session.isAllowSupervisorInJury()) {
			return;
		}
		Long supervisorId = project.getSupervisor().getId();
		for (Object m : members) {
			Long tid = null;
			if (m instanceof CreateJuryRequest.MemberEntry entry) {
				tid = entry.teacherId();
			} else if (m instanceof UpdateJuryRequest.MemberEntry entry) {
				tid = entry.teacherId();
			}
			if (tid != null && tid.equals(supervisorId)) {
				throw new InvalidBusinessStateException(
						"Un enseignant ne peut pas être à la fois encadrant et membre du jury pour le même projet");
			}
		}
	}

	@Audited(action = "DELETE", entity = "Defense")
	@Transactional
	public void cancelDefense(Long defenseId) {
		Defense defense = defenseRepository.findById(defenseId)
				.orElseThrow(() -> new EntityNotFoundException("Soutenance non trouvée"));

		defenseRepository.delete(defense);

		eventPublisher.publishEvent(new DefenseCancelledEvent(securityService.getCurrentUserEmail(), defense.getId(),
				defense.getDate(), defense.getTime()));
	}

	@Audited(action = "UPDATE", entity = "Jury")
	@Transactional
	public Defense clearJuryMembers(Long defenseId) {
		Defense defense = defenseRepository.findById(defenseId)
				.orElseThrow(() -> new EntityNotFoundException("Soutenance non trouvée"));
		defense.setMembers(new ArrayList<>());
		return defenseRepository.save(defense);
	}

	@Transactional
	public void publish(Long defenseSessionId) {
		DefenseSession ds = defenseSessionRepository.findById(defenseSessionId)
				.orElseThrow(() -> new EntityNotFoundException("Session de soutenance non trouvée"));
		if (ds.getStatus() == DefenseSessionStatus.ACTIVE) {
			if (ds.getApprovedBy() == null) {
				throw new InvalidBusinessStateException(
						"La session doit être approuvée par un administrateur avant d'être publiée");
			}
			ds.setStatus(DefenseSessionStatus.SCHEDULED);
			defenseSessionRepository.save(ds);
			eventPublisher.publishEvent(
					new DefenseSessionPublishedEvent(securityService.getCurrentUserEmail(), ds.getId(), ds.getName()));
		}
	}

	@Transactional(readOnly = true)
	public List<ScheduleResponse> autoGenerate(Long defenseSessionId) {
		DefenseSession ds = defenseSessionRepository.findById(defenseSessionId)
				.orElseThrow(() -> new EntityNotFoundException("Session de soutenance non trouvée"));

		if (ds.getStartTime() == null || ds.getEndTime() == null) {
			throw new InvalidBusinessStateException("Les horaires de la session ne sont pas configurés");
		}

		List<Room> rooms = roomRepository.findAll();
		if (rooms.isEmpty()) {
			throw new InvalidBusinessStateException("Aucune salle disponible");
		}

		LocalTime startTime = LocalTime.parse(ds.getStartTime());
		LocalTime endTime = LocalTime.parse(ds.getEndTime());
		int slotDuration = ds.getDefenseDuration();
		int breakMinutes = ds.getBreakDuration();

		List<Project> allProjects = projectRepository.findAll();

		// We now use DefenseRepository to check for juries (defenses with members)
		Set<Long> projectsWithJuries = defenseRepository.findAllWithMembers().stream()
				.filter(d -> d.getProject() != null && d.getMembers() != null && !d.getMembers().isEmpty())
				.map(d -> d.getProject().getId()).collect(Collectors.toSet());

		Map<Long, Integer> projectStudentCounts = new HashMap<>();
		List<Group> allGroups = groupRepository.findAll();

		for (Project p : allProjects) {
			int count = 0;
			var projectGroups = allGroups.stream()
					.filter(g -> g.getProject() != null && g.getProject().getId().equals(p.getId())).toList();
			if (!projectGroups.isEmpty()) {
				count = projectGroups.get(0).getStudents() != null ? projectGroups.get(0).getStudents().size() : 0;
			}
			projectStudentCounts.put(p.getId(), count);
		}

		List<Project> approvedProjects = allProjects.stream().filter(p -> p.getStatus() == ProjectStatus.APPROVED)
				.filter(p -> projectsWithJuries.contains(p.getId())).collect(Collectors.toList());

		if (approvedProjects.isEmpty()) {
			throw new InvalidBusinessStateException("Aucun projet approuvé avec jury");
		}

		Map<Long, List<String>> studentNamesByProject = new HashMap<>();
		Map<Long, List<Group>> groupsByProject = allGroups.stream().filter(g -> g.getProject() != null)
				.collect(Collectors.groupingBy(g -> g.getProject().getId()));
		for (Project p : allProjects) {
			studentNamesByProject.put(p.getId(), resolveStudentNames(p, groupsByProject.get(p.getId())));
		}

		List<ScheduleResponse> result = new ArrayList<>();
		Set<Long> assignedProjects = new HashSet<>();

		LocalDate current = ds.getStartDate();
		while (!current.isAfter(ds.getEndDate())) {
			for (Room room : rooms) {
				LocalTime time = startTime;
				while (time.plusMinutes(slotDuration).isBefore(endTime)
						|| time.plusMinutes(slotDuration).equals(endTime)) {
					LocalDate currentDate = current;
					LocalTime currentTime = time;

					for (Project project : approvedProjects) {
						if (!assignedProjects.contains(project.getId())
								&& projectStudentCounts.getOrDefault(project.getId(), 0) <= room.getCapacity()) {

							result.add(new ScheduleResponse(null, project.getTitle(), currentDate.toString(),
									currentTime.toString(), project.getId(), room.getId(), room.getName(),
									project.getTitle(), studentNamesByProject.getOrDefault(project.getId(), List.of()),
									"", "scheduled"));
							assignedProjects.add(project.getId());
							break;
						}
					}
					time = time.plusMinutes(slotDuration + breakMinutes);
				}
			}
			current = current.plusDays(1);
		}

		return result;
	}

	private List<String> resolveStudentNames(Project project, List<Group> projectGroups) {
		if (projectGroups != null && !projectGroups.isEmpty()) {
			var g = projectGroups.get(0);
			if (g.getStudents() != null)
				return g.getStudents().stream().map(s -> s.getFirstName() + " " + s.getLastName()).toList();
		}
		return List.of();
	}

	private void validateNoDuplicateTeachers(List<?> members) {
		Set<Long> teacherIds = new HashSet<>();
		for (Object m : members) {
			Long tid;
			if (m instanceof CreateJuryRequest.MemberEntry entry) {
				tid = entry.teacherId();
			} else if (m instanceof UpdateJuryRequest.MemberEntry entry) {
				tid = entry.teacherId();
			} else {
				continue;
			}
			if (tid == null) {
				continue;
			}
			if (!teacherIds.add(tid)) {
				throw new InvalidBusinessStateException(
						"Un enseignant ne peut être assigné qu'à un seul rôle dans un même jury");
			}
		}
	}
}