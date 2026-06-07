package com.system_gestion_soutenance.api.coordinator.document.service;

import com.system_gestion_soutenance.api.admin.config.general.entity.GeneralSettings;
import com.system_gestion_soutenance.api.admin.config.general.repository.GeneralSettingsRepository;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.coordinator.document.dto.*;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.jury.entity.Jury;
import com.system_gestion_soutenance.api.coordinator.jury.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.coordinator.schedule.entity.SlotAssignment;
import com.system_gestion_soutenance.api.coordinator.schedule.repository.SlotAssignmentRepository;
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

	public List<EvaluationSheetResponse> evaluationSheets(DefenseIdsRequest request) {
		List<Long> ids = resolveDefenseIds(request);
		List<EvaluationSheetResponse> result = new ArrayList<>();

		for (Long id : ids) {
			SlotAssignment slot = slotAssignmentRepository.findById(id)
					.orElseThrow(() -> new EntityNotFoundException("Soutenance non trouvée: " + id));

			Project project = slot.getProjectId() != null
					? projectRepository.findById(slot.getProjectId()).orElse(null)
					: null;
			if (project == null)
				continue;

			result.add(buildDefenseData(slot, project));
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
			SlotAssignment slot = slotAssignmentRepository.findById(id)
					.orElseThrow(() -> new EntityNotFoundException("Soutenance non trouvée: " + id));

			Project project = slot.getProjectId() != null
					? projectRepository.findById(slot.getProjectId()).orElse(null)
					: null;
			if (project == null)
				continue;

			List<Jury> juries = juryRepository.findByProjectId(project.getId());
			for (Jury jury : juries) {
				for (JuryMember member : jury.getMembers()) {
					result.add(new JuryConvocationResponse(
							member.getTeacher().getFirstName() + " " + member.getTeacher().getLastName(),
							member.getRoleName(), project.getTitle(), getStudentNames(project.getId()), slot.getDate(),
							slot.getTime(), slot.getRoom() != null ? slot.getRoom().getName() : null,
							findDefenseSessionName(project.getId())));
				}
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

	public ProcesVerbalResponse procesVerbal(Long projectId) {
		Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new EntityNotFoundException("Projet non trouvé: " + projectId));

		GeneralSettings generalSettings = generalSettingsRepository.findById(1L).orElse(null);
		ProcesVerbalResponse.Settings settings = generalSettings != null
				? new ProcesVerbalResponse.Settings(generalSettings.getInstitutionName(),
						generalSettings.getInstitutionLogoUrl(), generalSettings.getTimezone(),
						generalSettings.getDateFormat())
				: new ProcesVerbalResponse.Settings(null, null, null, null);

		ProcesVerbalResponse.GradeDetails grade = new ProcesVerbalResponse.GradeDetails(project.getId(),
				project.getTitle(), 0.0, "En attente");

		List<ProcesVerbalResponse.JuryMemberDetails> juryMembers = new ArrayList<>();
		List<Jury> juries = juryRepository.findByProjectId(projectId);
		for (Jury jury : juries) {
			for (JuryMember member : jury.getMembers()) {
				juryMembers.add(new ProcesVerbalResponse.JuryMemberDetails(member.getRoleName(),
						member.getTeacher().getFirstName() + " " + member.getTeacher().getLastName()));
			}
		}

		return new ProcesVerbalResponse(settings, grade, getStudentNames(projectId),
				project.getSupervisor() != null
						? project.getSupervisor().getFirstName() + " " + project.getSupervisor().getLastName()
						: null,
				juryMembers);
	}

	private List<Long> resolveDefenseIds(DefenseIdsRequest request) {
		if (request.projectId() != null) {
			List<SlotAssignment> slots = slotAssignmentRepository.findByProjectId(request.projectId());
			if (slots.isEmpty()) {
				throw new EntityNotFoundException("Aucune soutenance trouvée pour le projet: " + request.projectId());
			}
			return slots.stream().map(SlotAssignment::getId).toList();
		}
		return request.defenseIds();
	}

	private EvaluationSheetResponse buildDefenseData(SlotAssignment slot, Project project) {
		List<Jury> juries = juryRepository.findByProjectId(project.getId());
		List<EvaluationSheetResponse.JuryMemberResponse> juryMembers = new ArrayList<>();
		Map<String, Integer> coefficients = new LinkedHashMap<>();

		for (Jury jury : juries) {
			for (JuryMember member : jury.getMembers()) {
				juryMembers.add(new EvaluationSheetResponse.JuryMemberResponse(member.getRoleName(),
						member.getTeacher().getFirstName() + " " + member.getTeacher().getLastName(), 0));
			}
			if (jury.getTemplate() != null && jury.getTemplate().getRoles() != null) {
				jury.getTemplate().getRoles().forEach(r -> coefficients.put(r.getName(), r.getCoefficient()));
			}
		}

		return new EvaluationSheetResponse(project.getId(), project.getTitle(), getStudentNames(project.getId()),
				project.getSupervisor() != null
						? project.getSupervisor().getFirstName() + " " + project.getSupervisor().getLastName()
						: null,
				slot.getDate(), slot.getTime(), slot.getRoom() != null ? slot.getRoom().getName() : null, juryMembers,
				coefficients);
	}

	private List<SlotDetails> buildGroupedSlots() {
		List<SlotDetails> slots = new ArrayList<>();

		for (SlotAssignment slot : slotAssignmentRepository.findAll()) {
			if (slot.getProjectId() == null)
				continue;

			Project project = projectRepository.findById(slot.getProjectId()).orElse(null);
			if (project == null)
				continue;

			slots.add(new SlotDetails(slot.getDate(), slot.getTime(),
					slot.getRoom() != null ? slot.getRoom().getName() : null, project.getTitle(),
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
