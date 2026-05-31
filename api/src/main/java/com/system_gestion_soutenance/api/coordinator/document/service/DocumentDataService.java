package com.system_gestion_soutenance.api.coordinator.document.service;

import com.system_gestion_soutenance.api.admin.config.general.entity.GeneralSettings;
import com.system_gestion_soutenance.api.admin.config.general.repository.GeneralSettingsRepository;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.coordinator.document.dto.DefenseIdsRequest;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.jury.entity.Jury;
import com.system_gestion_soutenance.api.coordinator.jury.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.coordinator.schedule.entity.SlotAssignment;
import com.system_gestion_soutenance.api.coordinator.schedule.repository.SlotAssignmentRepository;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DocumentDataService {

	private final SlotAssignmentRepository slotAssignmentRepository;
	private final ProjectRepository projectRepository;
	private final JuryRepository juryRepository;
	private final GroupRepository groupRepository;
	private final DefenseSessionRepository defenseSessionRepository;
	private final GeneralSettingsRepository generalSettingsRepository;

	public DocumentDataService(SlotAssignmentRepository slotAssignmentRepository, ProjectRepository projectRepository,
			JuryRepository juryRepository, GroupRepository groupRepository,
			DefenseSessionRepository defenseSessionRepository, GeneralSettingsRepository generalSettingsRepository) {
		this.slotAssignmentRepository = slotAssignmentRepository;
		this.projectRepository = projectRepository;
		this.juryRepository = juryRepository;
		this.groupRepository = groupRepository;
		this.defenseSessionRepository = defenseSessionRepository;
		this.generalSettingsRepository = generalSettingsRepository;
	}

	public List<Map<String, Object>> evaluationSheets(DefenseIdsRequest request) {
		List<Long> ids = resolveDefenseIds(request);
		List<Map<String, Object>> result = new ArrayList<>();

		for (Long id : ids) {
			SlotAssignment slot = slotAssignmentRepository.findById(id).orElseThrow(
					() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Soutenance non trouvée: " + id));

			Project project = slot.getProjectId() != null
					? projectRepository.findById(slot.getProjectId()).orElse(null)
					: null;
			if (project == null)
				continue;

			Map<String, Object> entry = buildDefenseData(slot, project);
			result.add(entry);
		}

		return result;
	}

	public Map<String, Object> attendanceList(Long defenseSessionId) {
		DefenseSession ds = defenseSessionRepository.findById(defenseSessionId).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session de soutenance non trouvée"));

		List<Map<String, Object>> slots = buildGroupedSlots();

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("defenseSessionName", ds.getName());
		result.put("slots", slots);

		return result;
	}

	public List<Map<String, Object>> juryConvocations(DefenseIdsRequest request) {
		List<Long> ids = resolveDefenseIds(request);
		List<Map<String, Object>> result = new ArrayList<>();

		for (Long id : ids) {
			SlotAssignment slot = slotAssignmentRepository.findById(id).orElseThrow(
					() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Soutenance non trouvée: " + id));

			Project project = slot.getProjectId() != null
					? projectRepository.findById(slot.getProjectId()).orElse(null)
					: null;
			if (project == null)
				continue;

			List<Jury> juries = juryRepository.findByProjectId(project.getId());
			for (Jury jury : juries) {
				for (JuryMember member : jury.getMembers()) {
					Map<String, Object> conv = new LinkedHashMap<>();
					conv.put("teacherName",
							member.getTeacher().getFirstName() + " " + member.getTeacher().getLastName());
					conv.put("role", member.getRoleName());
					conv.put("projectTitle", project.getTitle());
					conv.put("studentNames", getStudentNames(project.getId()));
					conv.put("date", slot.getDate());
					conv.put("time", slot.getTime());
					conv.put("roomName", slot.getRoom() != null ? slot.getRoom().getName() : null);
					conv.put("defenseSessionName", findDefenseSessionName(project.getId()));
					result.add(conv);
				}
			}
		}

		return result;
	}

	public Map<String, Object> schedule(Long defenseSessionId) {
		DefenseSession ds = defenseSessionRepository.findById(defenseSessionId).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session de soutenance non trouvée"));

		List<Map<String, Object>> slots = buildGroupedSlots();

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("defenseSessionName", ds.getName());
		result.put("slots", slots);

		return result;
	}

	public Map<String, Object> procesVerbal(Long projectId) {
		Map<String, Object> result = new LinkedHashMap<>();

		Project project = projectRepository.findById(projectId).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projet non trouvé: " + projectId));

		GeneralSettings settings = generalSettingsRepository.findById(1L).orElse(null);
		Map<String, Object> settingsMap = new LinkedHashMap<>();
		if (settings != null) {
			settingsMap.put("institutionName", settings.getInstitutionName());
			settingsMap.put("institutionLogoUrl", settings.getInstitutionLogoUrl());
			settingsMap.put("timezone", settings.getTimezone());
			settingsMap.put("dateFormat", settings.getDateFormat());
		}
		result.put("settings", settingsMap);

		Map<String, Object> grade = new LinkedHashMap<>();
		grade.put("projectId", project.getId());
		grade.put("projectTitle", project.getTitle());
		grade.put("finalScore", 0);
		grade.put("decision", "En attente");
		result.put("grade", grade);

		result.put("studentNames", getStudentNames(projectId));
		result.put("supervisorName",
				project.getSupervisor() != null
						? project.getSupervisor().getFirstName() + " " + project.getSupervisor().getLastName()
						: null);

		List<Map<String, Object>> juryMembers = new ArrayList<>();
		List<Jury> juries = juryRepository.findByProjectId(projectId);
		for (Jury jury : juries) {
			for (JuryMember member : jury.getMembers()) {
				Map<String, Object> jm = new LinkedHashMap<>();
				jm.put("roleName", member.getRoleName());
				jm.put("teacherName", member.getTeacher().getFirstName() + " " + member.getTeacher().getLastName());
				juryMembers.add(jm);
			}
		}
		result.put("juryMembers", juryMembers);

		return result;
	}

	private List<Long> resolveDefenseIds(DefenseIdsRequest request) {
		if (request.projectId() != null) {
			List<SlotAssignment> slots = slotAssignmentRepository.findByProjectId(request.projectId());
			if (slots.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Aucune soutenance trouvée pour le projet: " + request.projectId());
			}
			return slots.stream().map(SlotAssignment::getId).toList();
		}
		return request.defenseIds();
	}

	private Map<String, Object> buildDefenseData(SlotAssignment slot, Project project) {
		Map<String, Object> entry = new LinkedHashMap<>();
		entry.put("projectId", project.getId());
		entry.put("projectTitle", project.getTitle());
		entry.put("studentNames", getStudentNames(project.getId()));
		entry.put("supervisorName",
				project.getSupervisor() != null
						? project.getSupervisor().getFirstName() + " " + project.getSupervisor().getLastName()
						: null);
		entry.put("date", slot.getDate());
		entry.put("time", slot.getTime());
		entry.put("roomName", slot.getRoom() != null ? slot.getRoom().getName() : null);

		List<Jury> juries = juryRepository.findByProjectId(project.getId());
		List<Map<String, Object>> juryMembers = new ArrayList<>();
		Map<String, Integer> coefficients = new LinkedHashMap<>();

		for (Jury jury : juries) {
			for (JuryMember member : jury.getMembers()) {
				Map<String, Object> jm = new LinkedHashMap<>();
				jm.put("roleName", member.getRoleName());
				jm.put("teacherName", member.getTeacher().getFirstName() + " " + member.getTeacher().getLastName());
				jm.put("coefficient", 0);
				juryMembers.add(jm);
			}
			if (jury.getTemplate() != null && jury.getTemplate().getRoles() != null) {
				jury.getTemplate().getRoles().forEach(r -> coefficients.put(r.getName(), r.getCoefficient()));
			}
		}

		entry.put("juryMembers", juryMembers);
		entry.put("evaluationCoefficients", coefficients);

		return entry;
	}

	private List<Map<String, Object>> buildGroupedSlots() {
		List<Map<String, Object>> slots = new ArrayList<>();

		for (SlotAssignment slot : slotAssignmentRepository.findAll()) {
			if (slot.getProjectId() == null)
				continue;

			Project project = projectRepository.findById(slot.getProjectId()).orElse(null);
			if (project == null)
				continue;

			Map<String, Object> s = new LinkedHashMap<>();
			s.put("date", slot.getDate());
			s.put("time", slot.getTime());
			s.put("roomName", slot.getRoom() != null ? slot.getRoom().getName() : null);
			s.put("projectTitle", project.getTitle());
			s.put("studentNames", getStudentNames(project.getId()));
			slots.add(s);
		}

		slots.sort(Comparator.comparing((Map<String, Object> a) -> (String) a.get("date"))
				.thenComparing(a -> (String) a.get("time")));

		return slots;
	}

	private List<String> getStudentNames(Long projectId) {
		List<Group> groups = groupRepository.findByProjectId(projectId);
		for (Group g : groups) {
			if (g.getStudents() != null && !g.getStudents().isEmpty()) {
				return g.getStudents().stream().map(s -> s.getFirstName() + " " + s.getLastName())
						.collect(Collectors.toList());
			}
		}
		Project project = projectRepository.findById(projectId).orElse(null);
		if (project != null && project.getStudents() != null) {
			return project.getStudents().stream().map(s -> s.getFirstName() + " " + s.getLastName())
					.collect(Collectors.toList());
		}
		return List.of();
	}

	private String findDefenseSessionName(Long projectId) {
		DefenseSession ds = findDefenseSession(projectId);
		return ds != null ? ds.getName() : null;
	}

	private DefenseSession findDefenseSession(Long projectId) {
		List<Group> groups = groupRepository.findByProjectId(projectId);
		for (Group g : groups) {
			if (g.getSessionId() != null) {
				return defenseSessionRepository.findById(g.getSessionId()).orElse(null);
			}
		}
		return null;
	}
}
