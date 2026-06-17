package com.system_gestion_soutenance.api.coordinator.conflict.service;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.defense.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.coordinator.unavailability.entity.Unavailability;
import com.system_gestion_soutenance.api.coordinator.unavailability.repository.UnavailabilityRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import com.system_gestion_soutenance.api.coordinator.conflict.dto.ConflictDetailResponse;
import com.system_gestion_soutenance.api.coordinator.conflict.dto.ConflictSlot;
import com.system_gestion_soutenance.api.coordinator.schedule.dto.ScheduleRequest;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
@SuppressWarnings("PMD")

@Service
public class ConflictDetectionService {

	private static final Logger LOG = LoggerFactory.getLogger(ConflictDetectionService.class);

	private final DefenseRepository defenseRepository;
	private final ProjectRepository projectRepository;
	private final GroupRepository groupRepository;
	private final UnavailabilityRepository unavailabilityRepository;
	private final DefenseSessionRepository defenseSessionRepository;

	public ConflictDetectionService(DefenseRepository defenseRepository, ProjectRepository projectRepository,
			GroupRepository groupRepository, UnavailabilityRepository unavailabilityRepository,
			DefenseSessionRepository defenseSessionRepository) {
		this.defenseRepository = defenseRepository;
		this.projectRepository = projectRepository;
		this.groupRepository = groupRepository;
		this.unavailabilityRepository = unavailabilityRepository;
		this.defenseSessionRepository = defenseSessionRepository;
	}

	public List<ConflictDetailResponse> validate(ScheduleRequest request, String defenseSessionId) {
		Map<String, ConflictSlot> mergedSchedule = new LinkedHashMap<>();

		int defenseDuration = resolveDefenseDuration(defenseSessionId);

		List<Defense> existingDefenses = defenseRepository.findAllWithMembers();
		for (Defense existing : existingDefenses) {
			String startTime = existing.getTime().toString();
			mergedSchedule.put(String.valueOf(existing.getId()),
					new ConflictSlot(String.valueOf(existing.getId()),
							existing.getProject() != null ? existing.getProject().getTitle() : "",
							existing.getDate().toString(), startTime, computeEndTime(startTime, defenseDuration),
							String.valueOf(existing.getProjectId()),
							existing.getRoom() != null ? String.valueOf(existing.getRoom().getId()) : null));
		}

		Map<Long, Set<String>> juryTeacherIdsByProject = precomputeJuryTeacherIds(existingDefenses);

		for (int i = 0; i < request.slots().size(); i++) {
			var slot = request.slots().get(i);
			String tempId = "new_" + i;
			mergedSchedule.put(tempId,
					new ConflictSlot(tempId, slot.title(), slot.date(), slot.time(),
							computeEndTime(slot.time(), defenseDuration),
							slot.projectId() == null ? null : String.valueOf(slot.projectId()),
							slot.roomId() == null ? null : String.valueOf(slot.roomId())));
		}

		return runAllChecks(mergedSchedule, defenseSessionId, juryTeacherIdsByProject);
	}

	private Map<Long, Set<String>> precomputeJuryTeacherIds(List<Defense> defenses) {
		Map<Long, Set<String>> map = new HashMap<>();
		for (Defense defense : defenses) {
			if (defense.getProject() == null) {
				continue;
			}
			Set<String> ids = new HashSet<>();
			for (JuryMember member : defense.getMembers()) {
				if (member.getTeacher() != null) {
					ids.add(String.valueOf(member.getTeacher().getId()));
				}
			}
			map.put(defense.getProject().getId(), ids);
		}
		return map;
	}

	private List<ConflictDetailResponse> runAllChecks(Map<String, ConflictSlot> schedule, String defenseSessionId,
			Map<Long, Set<String>> juryTeacherIdsByProject) {
		List<ConflictDetailResponse> conflicts = new ArrayList<>();

		conflicts.addAll(checkProjectAlreadyScheduled(schedule));
		conflicts.addAll(checkSlotOccupied(schedule));
		conflicts.addAll(checkDateOutOfBounds(schedule, defenseSessionId));
		conflicts.addAll(checkTeacherDoubleBooked(schedule, juryTeacherIdsByProject));
		conflicts.addAll(checkSupervisorConflict(schedule));
		conflicts.addAll(checkTeacherUnavailable(schedule, juryTeacherIdsByProject));
		conflicts.addAll(checkStudentDoubleBooked(schedule));

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
		Map<String, List<Map.Entry<String, ConflictSlot>>> byDateRoom = new HashMap<>();

		for (Map.Entry<String, ConflictSlot> entry : schedule.entrySet()) {
			String slotId = entry.getKey();
			ConflictSlot data = entry.getValue();
			String date = data.date();
			String roomId = data.roomId();
			String time = data.time();
			String endTime = data.endTime();
			if (date == null || roomId == null || time == null)
				continue;

			String key = date + "|" + roomId;
			List<Map.Entry<String, ConflictSlot>> existing = byDateRoom.getOrDefault(key, new ArrayList<>());
			for (Map.Entry<String, ConflictSlot> prev : existing) {
				ConflictSlot prevData = prev.getValue();
				if (timeRangesOverlap(time, endTime, prevData.time(), prevData.endTime())) {
					conflicts.add(createConflict("slot_occupied", "error",
							"Un autre projet occupe deja ce creneau (date: " + date + ", salle: " + roomId
									+ ", horaire: " + time + ")",
							slotId, "Choisissez une autre date, salle ou horaire"));
					break;
				}
			}
			existing.add(entry);
			byDateRoom.put(key, existing);
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
				LOG.warn("Invalid date format: {}", dateStr, e);
			}
		}
		return conflicts;
	}

	private List<ConflictDetailResponse> checkTeacherDoubleBooked(Map<String, ConflictSlot> schedule,
			Map<Long, Set<String>> juryTeacherIdsByProject) {
		List<ConflictDetailResponse> conflicts = new ArrayList<>();
		Map<String, List<Map.Entry<String, ConflictSlot>>> dateTeacherSlots = new HashMap<>();
		Set<String> reportedSlotIds = new HashSet<>();

		for (Map.Entry<String, ConflictSlot> entry : schedule.entrySet()) {
			String slotId = entry.getKey();
			ConflictSlot data = entry.getValue();
			String projectId = data.projectId();
			String date = data.date();
			String time = data.time();
			String endTime = data.endTime();
			if (projectId == null || date == null || time == null || endTime == null)
				continue;

			Set<String> teacherIds = juryTeacherIdsByProject.getOrDefault(Long.valueOf(projectId), Set.of());
			for (String tid : teacherIds) {
				String key = date + "|" + tid;
				List<Map.Entry<String, ConflictSlot>> existing = dateTeacherSlots.getOrDefault(key, new ArrayList<>());
				for (Map.Entry<String, ConflictSlot> prev : existing) {
					ConflictSlot prevData = prev.getValue();
					if (timeRangesOverlap(time, endTime, prevData.time(), prevData.endTime())) {
						if (!reportedSlotIds.contains(slotId)) {
							conflicts
									.add(createConflict("teacher_double_booked", "error",
											"Un enseignant est deja assigne a un autre projet le " + date + " de "
													+ time + " a " + endTime,
											slotId, "Verifiez la disponibilite des enseignants"));
							reportedSlotIds.add(slotId);
						}
						break;
					}
				}
				existing.add(entry);
				dateTeacherSlots.put(key, existing);
			}
		}
		return conflicts;
	}

	private List<ConflictDetailResponse> checkSupervisorConflict(Map<String, ConflictSlot> schedule) {
		List<ConflictDetailResponse> conflicts = new ArrayList<>();
		Map<String, List<Map.Entry<String, ConflictSlot>>> dateSupervisorSlots = new HashMap<>();

		for (Map.Entry<String, ConflictSlot> entry : schedule.entrySet()) {
			String slotId = entry.getKey();
			ConflictSlot data = entry.getValue();
			String projectId = data.projectId();
			String date = data.date();
			String time = data.time();
			String endTime = data.endTime();
			if (projectId == null || date == null || time == null || endTime == null)
				continue;

			Project project = projectRepository.findById(Long.valueOf(projectId)).orElse(null);
			if (project == null || project.getSupervisor() == null)
				continue;

			String supervisorId = String.valueOf(project.getSupervisor().getId());
			String key = date + "|" + supervisorId;
			List<Map.Entry<String, ConflictSlot>> existing = dateSupervisorSlots.getOrDefault(key, new ArrayList<>());
			for (Map.Entry<String, ConflictSlot> prev : existing) {
				ConflictSlot prevData = prev.getValue();
				if (timeRangesOverlap(time, endTime, prevData.time(), prevData.endTime())) {
					conflicts.add(createConflict(
							"supervisor_conflict", "warning", "L'encadrant est deja assigne a un autre projet le "
									+ date + " de " + time + " a " + endTime,
							slotId, "Verifiez la disponibilite de l'encadrant"));
					break;
				}
			}
			existing.add(entry);
			dateSupervisorSlots.put(key, existing);
		}
		return conflicts;
	}

	private List<ConflictDetailResponse> checkTeacherUnavailable(Map<String, ConflictSlot> schedule,
			Map<Long, Set<String>> juryTeacherIdsByProject) {
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

			Set<String> teacherIds = juryTeacherIdsByProject.getOrDefault(Long.valueOf(projectId), Set.of());
			for (String tid : teacherIds) {
				for (Unavailability ua : unavailabilityList) {
					if (!String.valueOf(ua.getTeacherId()).equals(tid) || !ua.getDate().toString().equals(date))
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

	private List<ConflictDetailResponse> checkStudentDoubleBooked(Map<String, ConflictSlot> schedule) {
		List<ConflictDetailResponse> conflicts = new ArrayList<>();
		Map<String, List<Map.Entry<String, ConflictSlot>>> dateStudentSlots = new HashMap<>();
		Set<String> reportedSlotIds = new HashSet<>();

		for (Map.Entry<String, ConflictSlot> entry : schedule.entrySet()) {
			String slotId = entry.getKey();
			ConflictSlot data = entry.getValue();
			String projectId = data.projectId();
			String date = data.date();
			String time = data.time();
			String endTime = data.endTime();
			if (projectId == null || date == null || time == null || endTime == null)
				continue;

			List<Group> groups = groupRepository.findByProjectId(Long.valueOf(projectId));
			for (Group group : groups) {
				if (group.getStudents() == null)
					continue;
				for (var student : group.getStudents()) {
					String studentId = String.valueOf(student.getId());
					String key = date + "|" + studentId;
					List<Map.Entry<String, ConflictSlot>> existing = dateStudentSlots.getOrDefault(key,
							new ArrayList<>());
					for (Map.Entry<String, ConflictSlot> prev : existing) {
						ConflictSlot prevData = prev.getValue();
						if (timeRangesOverlap(time, endTime, prevData.time(), prevData.endTime())) {
							if (!reportedSlotIds.contains(slotId)) {
								conflicts.add(createConflict("student_double_booked", "error",
										"Un etudiant est deja assigne a un autre projet le " + date + " de " + time
												+ " a " + endTime,
										slotId, "Verifiez l'assignation des etudiants aux projets"));
								reportedSlotIds.add(slotId);
							}
							break;
						}
					}
					existing.add(entry);
					dateStudentSlots.put(key, existing);
				}
			}
		}
		return conflicts;
	}

	private ConflictDetailResponse createConflict(String type, String severity, String message, String slot,
			String resolution) {
		return new ConflictDetailResponse(type, severity, message, slot, resolution);
	}

	private int resolveDefenseDuration(String defenseSessionId) {
		if (defenseSessionId != null) {
			DefenseSession ds = defenseSessionRepository.findById(Long.valueOf(defenseSessionId)).orElse(null);
			if (ds != null && ds.getDefenseDuration() > 0)
				return ds.getDefenseDuration();
		}
		return 60;
	}

	private String computeEndTime(String startTime, int durationMinutes) {
		if (startTime == null)
			return null;
		try {
			LocalTime start = LocalTime.parse(startTime);
			return start.plusMinutes(durationMinutes).toString();
		} catch (DateTimeParseException e) {
			LOG.warn("Invalid time format: {}", startTime, e);
			return null;
		}
	}

	private boolean timeRangesOverlap(String startA, String endA, String startB, String endB) {
		try {
			LocalTime a1 = LocalTime.parse(startA);
			LocalTime a2 = LocalTime.parse(endA);
			LocalTime b1 = LocalTime.parse(startB);
			LocalTime b2 = LocalTime.parse(endB);
			return a1.isBefore(b2) && b1.isBefore(a2);
		} catch (DateTimeParseException e) {
			return false;
		}
	}
}