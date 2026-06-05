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
import com.system_gestion_soutenance.api.coordinator.conflict.dto.ConflictDetailResponse;
import com.system_gestion_soutenance.api.coordinator.conflict.dto.ConflictSlot;
import com.system_gestion_soutenance.api.coordinator.schedule.dto.ScheduleRequest;
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

	public List<ConflictDetailResponse> validate(ScheduleRequest request, String defenseSessionId) {
		Map<String, ConflictSlot> mergedSchedule = new LinkedHashMap<>();

		for (SlotAssignment existing : slotAssignmentRepository.findAll()) {
			mergedSchedule.put(String.valueOf(existing.getId()),
					new ConflictSlot(String.valueOf(existing.getId()), existing.getTitle(), existing.getDate(),
							existing.getTime(), String.valueOf(existing.getProjectId()),
							existing.getRoom() != null ? String.valueOf(existing.getRoom().getId()) : null));
		}

		for (int i = 0; i < request.slots().size(); i++) {
			var slot = request.slots().get(i);
			String tempId = "new_" + i;
			mergedSchedule.put(tempId,
					new ConflictSlot(tempId, slot.title(), slot.date(), slot.time(),
							slot.projectId() == null ? null : String.valueOf(slot.projectId()),
							slot.roomId() == null ? null : String.valueOf(slot.roomId())));
		}

		return runAllChecks(mergedSchedule, defenseSessionId);
	}

	private List<ConflictDetailResponse> runAllChecks(Map<String, ConflictSlot> schedule, String defenseSessionId) {
		List<ConflictDetailResponse> conflicts = new ArrayList<>();

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

	private List<ConflictDetailResponse> checkProjectAlreadyScheduled(Map<String, ConflictSlot> schedule) {
		List<ConflictDetailResponse> conflicts = new ArrayList<>();
		Map<String, String> projectToSlot = new HashMap<>();

		for (Map.Entry<String, ConflictSlot> entry : schedule.entrySet()) {
			String slotId = entry.getKey();
			String projectId = entry.getValue().projectId();
			if (projectId == null)
				continue;

			if (projectToSlot.containsKey(projectId)) {
				conflicts.add(createConflict("project_already_scheduled", "error",
						"Le projet est deja planifie dans le creneau " + projectToSlot.get(projectId), slotId,
						"Supprimez l'ancien creneau ou choisissez un autre projet"));
			} else {
				projectToSlot.put(projectId, slotId);
			}
		}
		return conflicts;
	}

	private List<ConflictDetailResponse> checkSlotOccupied(Map<String, ConflictSlot> schedule) {
		List<ConflictDetailResponse> conflicts = new ArrayList<>();
		Set<String> seen = new HashSet<>();

		for (Map.Entry<String, ConflictSlot> entry : schedule.entrySet()) {
			String slotId = entry.getKey();
			ConflictSlot data = entry.getValue();
			String date = data.date();
			String roomId = data.roomId();
			String time = data.time();
			String key = date + "|" + roomId + "|" + time;

			if (seen.contains(key)) {
				conflicts.add(createConflict(
						"slot_occupied", "error", "Un autre projet occupe deja ce creneau (date: " + date + ", salle: "
								+ roomId + ", horaire: " + time + ")",
						slotId, "Choisissez une autre date, salle ou horaire"));
			}
			seen.add(key);
		}
		return conflicts;
	}

	private List<ConflictDetailResponse> checkRoomCapacity(Map<String, ConflictSlot> schedule) {
		List<ConflictDetailResponse> conflicts = new ArrayList<>();

		for (Map.Entry<String, ConflictSlot> entry : schedule.entrySet()) {
			String slotId = entry.getKey();
			ConflictSlot data = entry.getValue();
			String projectId = data.projectId();
			String roomId = data.roomId();
			if (projectId == null || roomId == null)
				continue;

			Room room = roomRepository.findById(Long.valueOf(roomId)).orElse(null);
			if (room == null)
				continue;

			int studentCount = getStudentCountForProject(projectId);
			if (studentCount > room.getCapacity()) {
				conflicts.add(createConflict("room_capacity", "error",
						"Capacite de la salle insuffisante: " + studentCount + " etudiants pour " + room.getCapacity()
								+ " places",
						slotId, "Choisissez une salle plus grande ou reduisez la taille du groupe"));
			}
		}
		return conflicts;
	}

	private List<ConflictDetailResponse> checkDateOutOfBounds(Map<String, ConflictSlot> schedule,
			String defenseSessionId) {
		List<ConflictDetailResponse> conflicts = new ArrayList<>();

		if (defenseSessionId == null)
			return conflicts;

		DefenseSession ds = defenseSessionRepository.findById(Long.valueOf(defenseSessionId)).orElse(null);
		if (ds == null)
			return conflicts;

		for (Map.Entry<String, ConflictSlot> entry : schedule.entrySet()) {
			String slotId = entry.getKey();
			String dateStr = entry.getValue().date();
			if (dateStr == null)
				continue;

			try {
				LocalDate date = LocalDate.parse(dateStr);
				if (date.isBefore(ds.getStartDate()) || date.isAfter(ds.getEndDate())) {
					conflicts.add(createConflict("out_of_bounds", "error",
							"La date " + dateStr + " est en dehors de la periode autorisee (" + ds.getStartDate()
									+ " - " + ds.getEndDate() + ")",
							slotId, "Choisissez une date entre " + ds.getStartDate() + " et " + ds.getEndDate()));
				}
			} catch (DateTimeParseException e) {
				log.warn("Invalid date format: {}", dateStr, e);
			}
		}
		return conflicts;
	}

	private List<ConflictDetailResponse> checkTeacherDoubleBooked(Map<String, ConflictSlot> schedule) {
		List<ConflictDetailResponse> conflicts = new ArrayList<>();
		Map<String, String> dateTeacherSlot = new HashMap<>();

		for (Map.Entry<String, ConflictSlot> entry : schedule.entrySet()) {
			String slotId = entry.getKey();
			ConflictSlot data = entry.getValue();
			String projectId = data.projectId();
			String date = data.date();
			if (projectId == null || date == null)
				continue;

			Set<String> teacherIds = getJuryTeacherIds(projectId);
			for (String tid : teacherIds) {
				String key = date + "|" + tid;
				if (dateTeacherSlot.containsKey(key)) {
					conflicts.add(createConflict("teacher_double_booked", "error",
							"Un enseignant est deja assigne a un autre projet le " + date, slotId,
							"Verifiez la disponibilite des enseignants"));
				} else {
					dateTeacherSlot.put(key, slotId);
				}
			}
		}
		return conflicts;
	}

	private List<ConflictDetailResponse> checkSupervisorConflict(Map<String, ConflictSlot> schedule) {
		List<ConflictDetailResponse> conflicts = new ArrayList<>();
		Map<String, String> dateSupervisorSlot = new HashMap<>();

		for (Map.Entry<String, ConflictSlot> entry : schedule.entrySet()) {
			String slotId = entry.getKey();
			ConflictSlot data = entry.getValue();
			String projectId = data.projectId();
			String date = data.date();
			if (projectId == null || date == null)
				continue;

			Project project = projectRepository.findById(Long.valueOf(projectId)).orElse(null);
			if (project == null || project.getSupervisor() == null)
				continue;

			String supervisorId = String.valueOf(project.getSupervisor().getId());
			String key = date + "|" + supervisorId;

			if (dateSupervisorSlot.containsKey(key)) {
				conflicts.add(createConflict("supervisor_conflict", "warning",
						"L'encadrant est deja assigne a un autre projet le " + date, slotId,
						"Verifiez la disponibilite de l'encadrant"));
			} else {
				dateSupervisorSlot.put(key, slotId);
			}
		}
		return conflicts;
	}

	private List<ConflictDetailResponse> checkBreakInterval(Map<String, ConflictSlot> schedule,
			String defenseSessionId) {
		List<ConflictDetailResponse> conflicts = new ArrayList<>();
		int breakDuration = 15;

		if (defenseSessionId != null) {
			DefenseSession ds = defenseSessionRepository.findById(Long.valueOf(defenseSessionId)).orElse(null);
			if (ds != null)
				breakDuration = ds.getBreakDuration();
		}

		Map<String, List<Map.Entry<String, ConflictSlot>>> byDateRoom = new HashMap<>();
		for (Map.Entry<String, ConflictSlot> entry : schedule.entrySet()) {
			ConflictSlot data = entry.getValue();
			String date = data.date();
			String roomId = data.roomId();
			String key = date + "|" + roomId;
			byDateRoom.computeIfAbsent(key, k -> new ArrayList<>()).add(entry);
		}

		for (Map.Entry<String, List<Map.Entry<String, ConflictSlot>>> group : byDateRoom.entrySet()) {
			List<Map.Entry<String, ConflictSlot>> slots = group.getValue();
			slots.sort(Comparator.comparing(e -> e.getValue().time()));

			for (int i = 1; i < slots.size(); i++) {
				String prevTime = slots.get(i - 1).getValue().time();
				String currTime = slots.get(i).getValue().time();
				if (prevTime == null || currTime == null)
					continue;

				try {
					long gap = ChronoUnit.MINUTES.between(LocalTime.parse(prevTime), LocalTime.parse(currTime));
					if (gap < breakDuration) {
						conflicts.add(createConflict("break_violation", "warning",
								"Intervalle insuffisant entre les creneaux: " + gap + " min au lieu de " + breakDuration
										+ " min",
								slots.get(i).getKey(), "Ajoutez un ecart d'au moins " + breakDuration + " minutes"));
					}
				} catch (DateTimeParseException e) {
					log.warn("Invalid time format: prev={}, curr={}", prevTime, currTime, e);
				}
			}
		}
		return conflicts;
	}

	private List<ConflictDetailResponse> checkTeacherUnavailable(Map<String, ConflictSlot> schedule) {
		List<ConflictDetailResponse> conflicts = new ArrayList<>();
		List<Unavailability> unavailabilityList = unavailabilityRepository.findAll();

		for (Map.Entry<String, ConflictSlot> entry : schedule.entrySet()) {
			String slotId = entry.getKey();
			ConflictSlot data = entry.getValue();
			String projectId = data.projectId();
			String date = data.date();
			String time = data.time();
			if (projectId == null || date == null || time == null)
				continue;

			Set<String> teacherIds = getJuryTeacherIds(projectId);
			for (String tid : teacherIds) {
				for (Unavailability ua : unavailabilityList) {
					if (!String.valueOf(ua.getTeacherId()).equals(tid) || !ua.getDate().equals(date))
						continue;
					if (ua.getSlots() != null && ua.getSlots().contains(time)) {
						conflicts.add(createConflict("teacher_unavailable", "error",
								"Un enseignant est indisponible le " + date + " a " + time, slotId,
								"Choisissez un autre creneau ou modifiez les indisponibilites"));
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

	private ConflictDetailResponse createConflict(String type, String severity, String message, String slot,
			String resolution) {
		return new ConflictDetailResponse(type, severity, message, slot, resolution);
	}
}
