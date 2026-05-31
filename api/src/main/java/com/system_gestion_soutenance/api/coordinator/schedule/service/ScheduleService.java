package com.system_gestion_soutenance.api.coordinator.schedule.service;

import com.system_gestion_soutenance.api.admin.config.settings.defense.entity.DefenseSettings;
import com.system_gestion_soutenance.api.admin.config.settings.defense.repository.DefenseSettingsRepository;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.admin.room.entity.Room;
import com.system_gestion_soutenance.api.admin.room.repository.RoomRepository;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.coordinator.schedule.entity.SlotAssignment;
import com.system_gestion_soutenance.api.coordinator.schedule.repository.SlotAssignmentRepository;
import com.system_gestion_soutenance.api.notification.entity.AppNotification;
import com.system_gestion_soutenance.api.notification.repository.NotificationRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

	public Map<String, Map<String, Object>> getSchedule() {
		List<SlotAssignment> slots = slotAssignmentRepository.findAll();
		Map<String, Map<String, Object>> result = new LinkedHashMap<>();
		for (SlotAssignment slot : slots) {
			result.put(slot.getId().toString(), toResponse(slot));
		}
		return result;
	}

	@Transactional
	public Map<String, Map<String, Object>> saveSchedule(Map<String, Map<String, Object>> schedule) {
		slotAssignmentRepository.deleteAll();

		for (Map.Entry<String, Map<String, Object>> entry : schedule.entrySet()) {
			Map<String, Object> data = entry.getValue();

			SlotAssignment slot = new SlotAssignment();
			slot.setTitle((String) data.get("title"));
			slot.setDate((String) data.get("date"));
			slot.setTime((String) data.get("time"));

			if (data.containsKey("projectId") && data.get("projectId") != null)
				slot.setProjectId(toLong(data.get("projectId")));

			if (data.containsKey("roomId") && data.get("roomId") != null) {
				Room room = roomRepository.findById(toLong(data.get("roomId")))
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
								"Salle introuvable: " + data.get("roomId")));
				slot.setRoom(room);
			}

			slotAssignmentRepository.save(slot);
		}

		return getSchedule();
	}

	public Map<String, Map<String, Object>> autoGenerate(Long defenseSessionId) {
		DefenseSession ds = defenseSessionRepository.findById(defenseSessionId).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session de soutenance non trouvée"));

		DefenseSettings settings = defenseSettingsRepository.findById(1L).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paramètres de soutenance non trouvés"));

		List<Room> rooms = roomRepository.findAll();
		if (rooms.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aucune salle disponible");
		}

		LocalTime startTime = LocalTime.parse(settings.getStartTime());
		LocalTime endTime = LocalTime.parse(settings.getEndTime());
		int slotDuration = ds.getDefenseDuration();
		int breakMinutes = ds.getBreakDuration();

		List<Project> approvedProjects = projectRepository.findAll().stream()
				.filter(p -> "approved".equals(p.getStatus()))
				.filter(p -> !juryRepository.findByProjectId(p.getId()).isEmpty()).collect(Collectors.toList());

		if (approvedProjects.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aucun projet approuvé avec jury");
		}

		Map<String, Map<String, Object>> schedule = new LinkedHashMap<>();
		Set<Long> assignedProjects = new HashSet<>();

		LocalDate current = ds.getStartDate();
		while (!current.isAfter(ds.getEndDate())) {
			for (Room room : rooms) {
				LocalTime time = startTime;
				while (time.plusMinutes(slotDuration).isBefore(endTime)
						|| time.plusMinutes(slotDuration).equals(endTime)) {

					LocalDate currentDate = current;
					LocalTime currentTime = time;
					approvedProjects.stream().filter(p -> !assignedProjects.contains(p.getId()))
							.filter(p -> getStudentCountForProject(p.getId()) <= room.getCapacity()).findFirst()
							.ifPresent(project -> {
								String slotId = UUID.randomUUID().toString();
								Map<String, Object> entry = new LinkedHashMap<>();
								entry.put("id", slotId);
								entry.put("title", project.getTitle());
								entry.put("date", currentDate.toString());
								entry.put("time", currentTime.toString());
								entry.put("projectId", project.getId());
								entry.put("roomId", room.getId());
								schedule.put(slotId, entry);
								assignedProjects.add(project.getId());
							});

					time = time.plusMinutes(slotDuration + breakMinutes);
				}
			}
			current = current.plusDays(1);
		}

		return schedule;
	}

	@Transactional
	public void publish(Long defenseSessionId) {
		DefenseSession ds = defenseSessionRepository.findById(defenseSessionId).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session de soutenance non trouvée"));

		if (ds.getStatus().name().equals("ACTIVE")) {
			ds.setStatus(com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSessionStatus.SCHEDULED);
			defenseSessionRepository.save(ds);
		}

		createNotification("success", "Soutenance publiée",
				"Le planning des soutenances pour " + ds.getName() + " a été publié.", "/coordinator/schedule");
	}

	@Transactional
	public void cancelDefense(Long slotId) {
		SlotAssignment slot = slotAssignmentRepository.findById(slotId).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Créneau de soutenance non trouvé"));

		slotAssignmentRepository.delete(slot);

		createNotification("warning", "Soutenance annulée", "La soutenance \"" + slot.getTitle() + "\" du "
				+ slot.getDate() + " à " + slot.getTime() + " a été annulée.", "/coordinator/schedule");
	}

	private void createNotification(String type, String title, String message, String actionLink) {
		AppNotification notification = new AppNotification();
		notification.setType(type);
		notification.setTitle(title);
		notification.setMessage(message);
		notification.setTimestamp(LocalDateTime.now());
		notification.setRead(false);
		notification.setActionLink(actionLink);
		notificationRepository.save(notification);
	}

	private int getStudentCountForProject(Long projectId) {
		var groups = groupRepository.findByProjectId(projectId);
		for (var g : groups) {
			if (g.getStudents() != null && !g.getStudents().isEmpty())
				return g.getStudents().size();
		}
		Project project = projectRepository.findById(projectId).orElse(null);
		if (project != null && project.getStudents() != null)
			return project.getStudents().size();
		return 0;
	}

	private Long toLong(Object value) {
		if (value instanceof Number)
			return ((Number) value).longValue();
		if (value instanceof String)
			return Long.parseLong((String) value);
		return null;
	}

	private Map<String, Object> toResponse(SlotAssignment slot) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("id", slot.getId());
		map.put("title", slot.getTitle());
		map.put("date", slot.getDate());
		map.put("time", slot.getTime());
		map.put("projectId", slot.getProjectId());
		map.put("roomId", slot.getRoom() != null ? slot.getRoom().getId() : null);
		return map;
	}
}
