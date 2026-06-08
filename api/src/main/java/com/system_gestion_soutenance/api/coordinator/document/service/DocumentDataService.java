package com.system_gestion_soutenance.api.coordinator.document.service;

import com.system_gestion_soutenance.api.admin.config.general.entity.GeneralSettings;
import com.system_gestion_soutenance.api.admin.config.general.repository.GeneralSettingsRepository;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.coordinator.document.dto.AttendanceListResponse;
import com.system_gestion_soutenance.api.coordinator.document.dto.DefenseIdsRequest;
import com.system_gestion_soutenance.api.coordinator.document.dto.EvaluationSheetResponse;
import com.system_gestion_soutenance.api.coordinator.document.dto.JuryConvocationResponse;
import com.system_gestion_soutenance.api.coordinator.document.dto.MinutesResponse;
import com.system_gestion_soutenance.api.coordinator.document.dto.ScheduleDocResponse;
import com.system_gestion_soutenance.api.coordinator.document.dto.SlotDetails;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.defense.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DocumentDataService {

	private final DefenseRepository defenseRepository;
	private final ProjectRepository projectRepository;
	private final GroupRepository groupRepository;
	private final DefenseSessionRepository defenseSessionRepository;
	private final GeneralSettingsRepository generalSettingsRepository;

	public DocumentDataService(DefenseRepository defenseRepository, ProjectRepository projectRepository,
			GroupRepository groupRepository, DefenseSessionRepository defenseSessionRepository,
			GeneralSettingsRepository generalSettingsRepository) {
		this.defenseRepository = defenseRepository;
		this.projectRepository = projectRepository;
		this.groupRepository = groupRepository;
		this.defenseSessionRepository = defenseSessionRepository;
		this.generalSettingsRepository = generalSettingsRepository;
	}

	public List<EvaluationSheetResponse> evaluationSheets(DefenseIdsRequest request) {
		List<Long> ids = resolveDefenseIds(request);
		List<EvaluationSheetResponse> result = new ArrayList<>();

		for (Long id : ids) {
			Defense defense = defenseRepository.findById(id)
					.orElseThrow(() -> new EntityNotFoundException("Soutenance non trouvée: " + id));

			Project project = defense.getProject();
			if (project == null)
				continue;

			result.add(buildDefenseData(defense, project));
		}

		return result;
	}

	public AttendanceListResponse attendanceList(Long defenseSessionId) {
		DefenseSession ds = defenseSessionRepository.findById(defenseSessionId)
				.orElseThrow(() -> new EntityNotFoundException("Session de soutenance non trouvée"));

		List<SlotDetails> slots = buildGroupedSlots();

		return new AttendanceListResponse(ds.getName(), slots);
	}

	public List<JuryConvocationResponse> juryConvocations(DefenseIdsRequest request) {
		List<Long> ids = resolveDefenseIds(request);
		List<JuryConvocationResponse> result = new ArrayList<>();

		for (Long id : ids) {
			Defense defense = defenseRepository.findById(id)
					.orElseThrow(() -> new EntityNotFoundException("Soutenance non trouvée: " + id));

			Project project = defense.getProject();
			if (project == null)
				continue;

			for (JuryMember member : defense.getMembers()) {
				result.add(new JuryConvocationResponse(
						member.getTeacher().getFirstName() + " " + member.getTeacher().getLastName(),
						member.getRoleName(), project.getTitle(), getStudentNames(project.getId()),
						defense.getDate().toString(), defense.getTime().toString(),
						defense.getRoom() != null ? defense.getRoom().getName() : null,
						findDefenseSessionName(project.getId())));
			}
		}

		return result;
	}

	public ScheduleDocResponse schedule(Long defenseSessionId) {
		DefenseSession ds = defenseSessionRepository.findById(defenseSessionId)
				.orElseThrow(() -> new EntityNotFoundException("Session de soutenance non trouvée"));

		List<SlotDetails> slots = buildGroupedSlots();

		return new ScheduleDocResponse(ds.getName(), slots);
	}

	public MinutesResponse minutes(Long projectId) {
		Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new EntityNotFoundException("Projet non trouvé: " + projectId));

		GeneralSettings generalSettings = generalSettingsRepository.findById(1L).orElse(null);
		MinutesResponse.Settings settings = generalSettings != null
				? new MinutesResponse.Settings(generalSettings.getInstitutionName(),
						generalSettings.getInstitutionLogoUrl(), generalSettings.getTimezone(),
						generalSettings.getDateFormat())
				: new MinutesResponse.Settings(null, null, null, null);

		MinutesResponse.GradeDetails grade = new MinutesResponse.GradeDetails(project.getId(), project.getTitle(), 0.0,
				"En attente");

		List<MinutesResponse.JuryMemberDetails> juryMembers = new ArrayList<>();
		defenseRepository.findByProject(project).ifPresent(defense -> {
			for (JuryMember member : defense.getMembers()) {
				juryMembers.add(new MinutesResponse.JuryMemberDetails(member.getRoleName(),
						member.getTeacher().getFirstName() + " " + member.getTeacher().getLastName()));
			}
		});

		return new MinutesResponse(settings, grade, getStudentNames(projectId),
				project.getSupervisor() != null
						? project.getSupervisor().getFirstName() + " " + project.getSupervisor().getLastName()
						: null,
				juryMembers);
	}

	private List<Long> resolveDefenseIds(DefenseIdsRequest request) {
		if (request.projectId() != null) {
			Project project = projectRepository.findById(request.projectId()).orElse(null);
			if (project == null) {
				throw new EntityNotFoundException("Projet introuvable: " + request.projectId());
			}
			return defenseRepository.findByProject(project).map(d -> List.of(d.getId()))
					.orElseThrow(() -> new EntityNotFoundException(
							"Aucune soutenance trouvée pour le projet: " + request.projectId()));
		}
		return request.defenseIds();
	}

	private EvaluationSheetResponse buildDefenseData(Defense defense, Project project) {
		List<JuryMember> members = defense.getMembers();
		List<EvaluationSheetResponse.JuryMemberResponse> juryMembers = new ArrayList<>();
		Map<String, Integer> coefficients = new LinkedHashMap<>();

		for (JuryMember member : members) {
			juryMembers.add(new EvaluationSheetResponse.JuryMemberResponse(member.getRoleName(),
					member.getTeacher().getFirstName() + " " + member.getTeacher().getLastName(), 0));
		}

		return new EvaluationSheetResponse(project.getId(), project.getTitle(), getStudentNames(project.getId()),
				project.getSupervisor() != null
						? project.getSupervisor().getFirstName() + " " + project.getSupervisor().getLastName()
						: null,
				defense.getDate().toString(), defense.getTime().toString(),
				defense.getRoom() != null ? defense.getRoom().getName() : null, juryMembers, coefficients);
	}

	private List<SlotDetails> buildGroupedSlots() {
		List<SlotDetails> slots = new ArrayList<>();

		for (Defense defense : defenseRepository.findAllWithMembers()) {
			if (defense.getProject() == null)
				continue;

			Project project = defense.getProject();

			slots.add(new SlotDetails(defense.getDate().toString(), defense.getTime().toString(),
					defense.getRoom() != null ? defense.getRoom().getName() : null, project.getTitle(),
					getStudentNames(project.getId())));
		}

		slots.sort(Comparator.comparing(SlotDetails::date).thenComparing(SlotDetails::time));

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
