package com.system_gestion_soutenance.api.coordinator.schedule.service;

import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.admin.config.settings.defense.entity.DefenseSettings;
import com.system_gestion_soutenance.api.admin.config.settings.defense.repository.DefenseSettingsRepository;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSessionStatus;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.admin.room.entity.Room;
import com.system_gestion_soutenance.api.admin.room.repository.RoomRepository;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.entity.ProjectStatus;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.coordinator.schedule.dto.ScheduleRequest;
import com.system_gestion_soutenance.api.coordinator.schedule.dto.ScheduleResponse;
import com.system_gestion_soutenance.api.coordinator.schedule.entity.SlotAssignment;
import com.system_gestion_soutenance.api.coordinator.schedule.repository.SlotAssignmentRepository;
import com.system_gestion_soutenance.api.notification.entity.AppNotification;
import com.system_gestion_soutenance.api.notification.entity.NotificationType;
import com.system_gestion_soutenance.api.notification.repository.NotificationRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleService {

	private final SlotAssignmentRepository slotAssignmentRepository;
	private final RoomRepository roomRepository;
	private final DefenseSessionRepository defenseSessionRepository;
	private final DefenseSettingsRepository defenseSettingsRepository;
	private final ProjectRepository projectRepository;
	private final JuryRepository juryRepository;
	private final GroupRepository groupRepository;
	private final NotificationRepository notificationRepository;

	public ScheduleService(SlotAssignmentRepository slotAssignmentRepository, RoomRepository roomRepository,
			DefenseSessionRepository defenseSessionRepository, DefenseSettingsRepository defenseSettingsRepository,
			ProjectRepository projectRepository, JuryRepository juryRepository, GroupRepository groupRepository,
			NotificationRepository notificationRepository) {
		this.slotAssignmentRepository = slotAssignmentRepository;
		this.roomRepository = roomRepository;
		this.defenseSessionRepository = defenseSessionRepository;
		this.defenseSettingsRepository = defenseSettingsRepository;
		this.projectRepository = projectRepository;
		this.juryRepository = juryRepository;
		this.groupRepository = groupRepository;
		this.notificationRepository = notificationRepository;
	}

	@Transactional(readOnly = true)
	public List<SlotAssignment> getSchedule() {
		return slotAssignmentRepository.findAllWithRoom();
	}

	public Map<Long, Project> buildProjectMap(List<SlotAssignment> slots) {
		List<Long> projectIds = slots.stream().map(SlotAssignment::getProjectId).filter(Objects::nonNull).distinct()
				.toList();
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

	private List<String> resolveStudentNames(Project project, List<Group> projectGroups) {
		if (projectGroups != null && !projectGroups.isEmpty()) {
			var g = projectGroups.get(0);
			if (g.getStudents() != null)
				return g.getStudents().stream().map(s -> s.getFirstName() + " " + s.getLastName()).toList();
		}
		if (project.getStudents() != null)
			return project.getStudents().stream().map(s -> s.getFirstName() + " " + s.getLastName()).toList();
		return List.of();
	}

	@Audited(action = "BULK_CREATE", entity = "SlotAssignment")
	@Transactional
	public List<SlotAssignment> saveSchedule(ScheduleRequest request) {
		slotAssignmentRepository.deleteAll();

		for (var slotReq : request.slots()) {
			SlotAssignment slot = new SlotAssignment();
			slot.setTitle(slotReq.title());
			slot.setDate(slotReq.date());
			slot.setTime(slotReq.time());

			if (slotReq.projectId() != null)
				slot.setProjectId(slotReq.projectId());

			if (slotReq.roomId() != null) {
				Room room = roomRepository.findById(slotReq.roomId())
						.orElseThrow(() -> new InvalidBusinessStateException("Salle introuvable: " + slotReq.roomId()));
				slot.setRoom(room);
			}

			slotAssignmentRepository.save(slot);
		}

		return getSchedule();
	}

	@Transactional(readOnly = true)
	public List<ScheduleResponse> autoGenerate(Long defenseSessionId) {
		DefenseSession ds = defenseSessionRepository.findById(defenseSessionId)
				.orElseThrow(() -> new EntityNotFoundException("Session de soutenance non trouvée"));

		DefenseSettings settings = defenseSettingsRepository.findFirstByOrderByIdAsc()
				.orElseThrow(() -> new EntityNotFoundException("Paramètres de soutenance non trouvés"));

		List<Room> rooms = roomRepository.findAll();
		if (rooms.isEmpty()) {
			throw new InvalidBusinessStateException("Aucune salle disponible");
		}

		LocalTime startTime = LocalTime.parse(settings.getStartTime());
		LocalTime endTime = LocalTime.parse(settings.getEndTime());
		int slotDuration = ds.getDefenseDuration();
		int breakMinutes = ds.getBreakDuration();

		List<Project> allProjects = projectRepository.findAll();
		Set<Long> projectsWithJuries = juryRepository.findAll().stream().map(jury -> jury.getProject().getId())
				.collect(Collectors.toSet());

		Map<Long, Integer> projectStudentCounts = new HashMap<>();
		List<Group> allGroups = groupRepository.findAll();

		for (Project p : allProjects) {
			int count = 0;
			var projectGroups = allGroups.stream()
					.filter(g -> g.getProject() != null && g.getProject().getId().equals(p.getId())).toList();
			if (!projectGroups.isEmpty()) {
				count = projectGroups.get(0).getStudents() != null ? projectGroups.get(0).getStudents().size() : 0;
			} else if (p.getStudents() != null) {
				count = p.getStudents().size();
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

							result.add(new ScheduleResponse(null, // Temporary ID
									project.getTitle(), currentDate.toString(), currentTime.toString(), project.getId(),
									room.getId(), room.getName(), project.getTitle(),
									studentNamesByProject.getOrDefault(project.getId(), List.of()), "", "scheduled"));
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

	@Audited(action = "UPDATE", entity = "DefenseSession")
	@Transactional
	public void publish(Long defenseSessionId) {
		DefenseSession ds = defenseSessionRepository.findById(defenseSessionId)
				.orElseThrow(() -> new EntityNotFoundException("Session de soutenance non trouvée"));

		if (ds.getStatus() == DefenseSessionStatus.ACTIVE) {
			ds.setStatus(DefenseSessionStatus.SCHEDULED);
			defenseSessionRepository.save(ds);
		}

		createNotification(NotificationType.SUCCESS, "Soutenance publiée",
				"Le planning des soutenances pour " + ds.getName() + " a été publié.", "/coordinator/schedule");
	}

	@Audited(action = "DELETE", entity = "SlotAssignment")
	@Transactional
	public void cancelDefense(Long slotId) {
		SlotAssignment slot = slotAssignmentRepository.findById(slotId)
				.orElseThrow(() -> new EntityNotFoundException("Créneau de soutenance non trouvé"));

		slotAssignmentRepository.delete(slot);

		createNotification(NotificationType.WARNING, "Soutenance annulée", "La soutenance \"" + slot.getTitle()
				+ "\" du " + slot.getDate() + " à " + slot.getTime() + " a été annulée.", "/coordinator/schedule");
	}

	private void createNotification(NotificationType type, String title, String message, String actionLink) {
		AppNotification notification = new AppNotification();
		notification.setType(type);
		notification.setTitle(title);
		notification.setMessage(message);
		notification.setTimestamp(LocalDateTime.now());
		notification.setRead(false);
		notification.setActionLink(actionLink);
		notificationRepository.save(notification);
	}

}
