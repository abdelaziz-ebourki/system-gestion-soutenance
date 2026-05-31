package com.system_gestion_soutenance.api.coordinator.conflict.service;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.admin.room.entity.Room;
import com.system_gestion_soutenance.api.admin.room.repository.RoomRepository;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.jury.entity.Jury;
import com.system_gestion_soutenance.api.coordinator.jury.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.coordinator.schedule.entity.SlotAssignment;
import com.system_gestion_soutenance.api.coordinator.schedule.repository.SlotAssignmentRepository;
import com.system_gestion_soutenance.api.coordinator.unavailability.entity.Unavailability;
import com.system_gestion_soutenance.api.coordinator.unavailability.repository.UnavailabilityRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ConflictDetectionService {

	private static final Logger log = LoggerFactory.getLogger(ConflictDetectionService.class);

	private final SlotAssignmentRepository slotAssignmentRepository;
	private final RoomRepository roomRepository;
	private final GroupRepository groupRepository;
	private final ProjectRepository projectRepository;
	private final JuryRepository juryRepository;
	private final UnavailabilityRepository unavailabilityRepository;
	private final DefenseSessionRepository defenseSessionRepository;

	public ConflictDetectionService(SlotAssignmentRepository slotAssignmentRepository, RoomRepository roomRepository,
			GroupRepository groupRepository, ProjectRepository projectRepository, JuryRepository juryRepository,
			UnavailabilityRepository unavailabilityRepository, DefenseSessionRepository defenseSessionRepository) {
		this.slotAssignmentRepository = slotAssignmentRepository;
		this.roomRepository = roomRepository;
		this.groupRepository = groupRepository;
		this.projectRepository = projectRepository;
		this.juryRepository = juryRepository;
		this.unavailabilityRepository = unavailabilityRepository;
		this.defenseSessionRepository = defenseSessionRepository;
	}

	public List<Map<String, Object>> validate(Map<String, Map<String, Object>> proposedSchedule,
			String defenseSessionId) {
		Map<String, Map<String, Object>> mergedSchedule = new LinkedHashMap<>();

		for (SlotAssignment existing : slotAssignmentRepository.findAll()) {
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put("id", existing.getId());
			entry.put("title", existing.getTitle());
			entry.put("date", existing.getDate());
			entry.put("time", existing.getTime());
			entry.put("projectId", String.valueOf(existing.getProjectId()));
			entry.put("roomId", existing.getRoom() != null ? String.valueOf(existing.getRoom().getId()) : null);
			mergedSchedule.put(String.valueOf(existing.getId()), entry);
		}

		mergedSchedule.putAll(proposedSchedule);

		return runAllChecks(mergedSchedule, defenseSessionId);
	}

	private List<Map<String, Object>> runAllChecks(Map<String, Map<String, Object>> schedule, String defenseSessionId) {
		List<Map<String, Object>> conflicts = new ArrayList<>();

		conflicts.addAll(checkProjectAlreadyScheduled(schedule));
		conflicts.addAll(checkSlotOccupied(schedule));
		conflicts.addAll(checkRoomCapacity(schedule));
		conflicts.addAll(checkDateOutOfBounds(schedule, defenseSessionId));
		conflicts.addAll(checkTeacherDoubleBooked(schedule));
		conflicts.addAll(checkSupervisorConflict(schedule));
		conflicts.addAll(checkBreakInterval(schedule, defenseSessionId));
		conflicts.addAll(checkTeacherUnavailable(schedule));

		return conflicts;
	}

	private List<Map<String, Object>> checkProjectAlreadyScheduled(Map<String, Map<String, Object>> schedule) {
		List<Map<String, Object>> conflicts = new ArrayList<>();
		Map<String, String> projectToSlot = new HashMap<>();

		for (Map.Entry<String, Map<String, Object>> entry : schedule.entrySet()) {
			String slotId = entry.getKey();
			String projectId = (String) entry.getValue().get("projectId");
			if (projectId == null)
				continue;

			if (projectToSlot.containsKey(projectId)) {
				conflicts.add(createConflict("project_already_scheduled", "error",
						"Le projet est déjà planifié dans le créneau " + projectToSlot.get(projectId), slotId,
						"Supprimez l'ancien créneau ou choisissez un autre projet"));
			} else {
				projectToSlot.put(projectId, slotId);
			}
		}
		return conflicts;
	}

	private List<Map<String, Object>> checkSlotOccupied(Map<String, Map<String, Object>> schedule) {
		List<Map<String, Object>> conflicts = new ArrayList<>();
		Set<String> seen = new HashSet<>();

		for (Map.Entry<String, Map<String, Object>> entry : schedule.entrySet()) {
			String slotId = entry.getKey();
			Map<String, Object> data = entry.getValue();
			String date = (String) data.get("date");
			String roomId = (String) data.get("roomId");
			String time = (String) data.get("time");
			String key = date + "|" + roomId + "|" + time;

			if (seen.contains(key)) {
				conflicts.add(createConflict(
						"slot_occupied", "error", "Un autre projet occupe déjà ce créneau (date: " + date + ", salle: "
								+ roomId + ", horaire: " + time + ")",
						slotId, "Choisissez une autre date, salle ou horaire"));
			}
			seen.add(key);
		}
		return conflicts;
	}

	private List<Map<String, Object>> checkRoomCapacity(Map<String, Map<String, Object>> schedule) {
		List<Map<String, Object>> conflicts = new ArrayList<>();

		for (Map.Entry<String, Map<String, Object>> entry : schedule.entrySet()) {
			String slotId = entry.getKey();
			Map<String, Object> data = entry.getValue();
			String projectId = (String) data.get("projectId");
			String roomId = (String) data.get("roomId");
			if (projectId == null || roomId == null)
				continue;

			Room room = roomRepository.findById(Long.valueOf(roomId)).orElse(null);
			if (room == null)
				continue;

			int studentCount = getStudentCountForProject(projectId);
			if (studentCount > room.getCapacity()) {
				conflicts.add(createConflict("room_capacity", "error",
						"Capacité de la salle insuffisante: " + studentCount + " étudiants pour " + room.getCapacity()
								+ " places",
						slotId, "Choisissez une salle plus grande ou réduisez la taille du groupe"));
			}
		}
		return conflicts;
	}

	private List<Map<String, Object>> checkDateOutOfBounds(Map<String, Map<String, Object>> schedule,
			String defenseSessionId) {
		List<Map<String, Object>> conflicts = new ArrayList<>();
		if (defenseSessionId == null)
			return conflicts;

		DefenseSession ds = defenseSessionRepository.findById(Long.valueOf(defenseSessionId)).orElse(null);
		if (ds == null)
			return conflicts;

		for (Map.Entry<String, Map<String, Object>> entry : schedule.entrySet()) {
			String slotId = entry.getKey();
			String dateStr = (String) entry.getValue().get("date");
			if (dateStr == null)
				continue;

			try {
				LocalDate date = LocalDate.parse(dateStr);
				if (date.isBefore(ds.getStartDate()) || date.isAfter(ds.getEndDate())) {
					conflicts.add(createConflict("out_of_bounds", "error",
							"La date " + dateStr + " est en dehors de la période autorisée (" + ds.getStartDate()
									+ " – " + ds.getEndDate() + ")",
							slotId, "Choisissez une date entre " + ds.getStartDate() + " et " + ds.getEndDate()));
				}
			} catch (DateTimeParseException e) {
				log.warn("Invalid date format: {}", dateStr, e);
			}
		}
		return conflicts;
	}

	private List<Map<String, Object>> checkTeacherDoubleBooked(Map<String, Map<String, Object>> schedule) {
		List<Map<String, Object>> conflicts = new ArrayList<>();
		Map<String, Map<String, String>> dateTeacherSlot = new HashMap<>();

		for (Map.Entry<String, Map<String, Object>> entry : schedule.entrySet()) {
			String slotId = entry.getKey();
			Map<String, Object> data = entry.getValue();
			String projectId = (String) data.get("projectId");
			String date = (String) data.get("date");
			if (projectId == null || date == null)
				continue;

			Set<String> teacherIds = getJuryTeacherIds(projectId);
			for (String tid : teacherIds) {
				String key = date + "|" + tid;
				if (dateTeacherSlot.containsKey(key)) {
					conflicts.add(createConflict("teacher_double_booked", "error",
							"Un enseignant est déjà assigné à un autre projet le " + date, slotId,
							"Vérifiez la disponibilité des enseignants"));
				} else {
					Map<String, String> val = new HashMap<>();
					val.put("slotId", slotId);
					dateTeacherSlot.put(key, val);
				}
			}
		}
		return conflicts;
	}

	private List<Map<String, Object>> checkSupervisorConflict(Map<String, Map<String, Object>> schedule) {
		List<Map<String, Object>> conflicts = new ArrayList<>();
		Map<String, String> dateSupervisorSlot = new HashMap<>();

		for (Map.Entry<String, Map<String, Object>> entry : schedule.entrySet()) {
			String slotId = entry.getKey();
			Map<String, Object> data = entry.getValue();
			String projectId = (String) data.get("projectId");
			String date = (String) data.get("date");
			if (projectId == null || date == null)
				continue;

			Project project = projectRepository.findById(Long.valueOf(projectId)).orElse(null);
			if (project == null || project.getSupervisor() == null)
				continue;

			String supervisorId = String.valueOf(project.getSupervisor().getId());
			String key = date + "|" + supervisorId;

			if (dateSupervisorSlot.containsKey(key)) {
				conflicts.add(createConflict("supervisor_conflict", "warning",
						"L'encadrant est déjà assigné à un autre projet le " + date, slotId,
						"Vérifiez la disponibilité de l'encadrant"));
			} else {
				dateSupervisorSlot.put(key, slotId);
			}
		}
		return conflicts;
	}

	private List<Map<String, Object>> checkBreakInterval(Map<String, Map<String, Object>> schedule,
			String defenseSessionId) {
		List<Map<String, Object>> conflicts = new ArrayList<>();
		int breakDuration = 15;

		if (defenseSessionId != null) {
			DefenseSession ds = defenseSessionRepository.findById(Long.valueOf(defenseSessionId)).orElse(null);
			if (ds != null)
				breakDuration = ds.getBreakDuration();
		}

		Map<String, List<Map.Entry<String, Map<String, Object>>>> byDateRoom = new HashMap<>();
		for (Map.Entry<String, Map<String, Object>> entry : schedule.entrySet()) {
			Map<String, Object> data = entry.getValue();
			String date = (String) data.get("date");
			String roomId = (String) data.get("roomId");
			String key = date + "|" + roomId;
			byDateRoom.computeIfAbsent(key, k -> new ArrayList<>()).add(entry);
		}

		for (Map.Entry<String, List<Map.Entry<String, Map<String, Object>>>> group : byDateRoom.entrySet()) {
			List<Map.Entry<String, Map<String, Object>>> slots = group.getValue();
			slots.sort(Comparator.comparing(e -> (String) e.getValue().get("time")));

			for (int i = 1; i < slots.size(); i++) {
				String prevTime = (String) slots.get(i - 1).getValue().get("time");
				String currTime = (String) slots.get(i).getValue().get("time");
				if (prevTime == null || currTime == null)
					continue;

				try {
					long gap = ChronoUnit.MINUTES.between(LocalTime.parse(prevTime), LocalTime.parse(currTime));
					if (gap < breakDuration) {
						conflicts.add(createConflict("break_violation", "warning",
								"Intervalle insuffisant entre les créneaux: " + gap + " min au lieu de " + breakDuration
										+ " min",
								slots.get(i).getKey(), "Ajoutez un écart d'au moins " + breakDuration + " minutes"));
					}
				} catch (DateTimeParseException e) {
					log.warn("Invalid time format: prev={}, curr={}", prevTime, currTime, e);
				}
			}
		}
		return conflicts;
	}

	private List<Map<String, Object>> checkTeacherUnavailable(Map<String, Map<String, Object>> schedule) {
		List<Map<String, Object>> conflicts = new ArrayList<>();
		List<Unavailability> unavailabilityList = unavailabilityRepository.findAll();

		for (Map.Entry<String, Map<String, Object>> entry : schedule.entrySet()) {
			String slotId = entry.getKey();
			Map<String, Object> data = entry.getValue();
			String projectId = (String) data.get("projectId");
			String date = (String) data.get("date");
			String time = (String) data.get("time");
			if (projectId == null || date == null || time == null)
				continue;

			Set<String> teacherIds = getJuryTeacherIds(projectId);
			for (String tid : teacherIds) {
				for (Unavailability ua : unavailabilityList) {
					if (!String.valueOf(ua.getTeacherId()).equals(tid) || !ua.getDate().equals(date))
						continue;
					if (ua.getSlots() != null && ua.getSlots().contains(time)) {
						conflicts.add(createConflict("teacher_unavailable", "error",
								"Un enseignant est indisponible le " + date + " à " + time, slotId,
								"Choisissez un autre créneau ou modifiez les indisponibilités"));
					}
				}
			}
		}
		return conflicts;
	}

	private int getStudentCountForProject(String projectId) {
		var groups = groupRepository.findByProjectId(Long.valueOf(projectId));
		for (var g : groups) {
			if (g.getStudents() != null && !g.getStudents().isEmpty())
				return g.getStudents().size();
		}
		Project project = projectRepository.findById(Long.valueOf(projectId)).orElse(null);
		if (project != null && project.getStudents() != null)
			return project.getStudents().size();
		return 0;
	}

	private Set<String> getJuryTeacherIds(String projectId) {
		Set<String> ids = new HashSet<>();
		for (Jury jury : juryRepository.findByProjectId(Long.valueOf(projectId))) {
			for (JuryMember member : jury.getMembers()) {
				if (member.getTeacher() != null) {
					ids.add(String.valueOf(member.getTeacher().getId()));
				}
			}
		}
		return ids;
	}

	private Map<String, Object> createConflict(String type, String severity, String message, String slot,
			String resolution) {
		Map<String, Object> conflict = new LinkedHashMap<>();
		conflict.put("type", type);
		conflict.put("severity", severity);
		conflict.put("message", message);
		conflict.put("slot", slot);
		conflict.put("suggestedResolution", resolution);
		return conflict;
	}
}
