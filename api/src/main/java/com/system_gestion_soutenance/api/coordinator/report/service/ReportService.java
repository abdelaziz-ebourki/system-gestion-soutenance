package com.system_gestion_soutenance.api.coordinator.report.service;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.defense.entity.DefenseStatus;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.coordinator.report.dto.GradeHistoryResponse;
import com.system_gestion_soutenance.api.coordinator.report.dto.GradeHistoryResponse.EvaluationEntry;
import com.system_gestion_soutenance.api.coordinator.report.dto.SessionReportResponse;
import com.system_gestion_soutenance.api.coordinator.report.dto.SessionReportResponse.DefenseReportDetails;
import com.system_gestion_soutenance.api.coordinator.report.dto.TeacherWorkloadResponse;
import com.system_gestion_soutenance.api.coordinator.report.entity.GeneratedDocument;
import com.system_gestion_soutenance.api.coordinator.report.repository.GeneratedDocumentRepository;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.Evaluation;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.EvaluationStatus;
import com.system_gestion_soutenance.api.teacher.evaluation.repository.EvaluationRepository;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@SuppressWarnings("PMD")

@Service
public class ReportService {

	private final DefenseSessionRepository defenseSessionRepository;
	private final DefenseRepository defenseRepository;
	private final GroupRepository groupRepository;
	private final ProjectRepository projectRepository;
	private final EvaluationRepository evaluationRepository;
	private final TeacherRepository teacherRepository;
	private final GeneratedDocumentRepository generatedDocumentRepository;
	private final SecurityService securityService;

	public ReportService(DefenseSessionRepository defenseSessionRepository, DefenseRepository defenseRepository,
			GroupRepository groupRepository, ProjectRepository projectRepository,
			EvaluationRepository evaluationRepository, TeacherRepository teacherRepository,
			GeneratedDocumentRepository generatedDocumentRepository, SecurityService securityService) {
		this.defenseSessionRepository = defenseSessionRepository;
		this.defenseRepository = defenseRepository;
		this.groupRepository = groupRepository;
		this.projectRepository = projectRepository;
		this.evaluationRepository = evaluationRepository;
		this.teacherRepository = teacherRepository;
		this.generatedDocumentRepository = generatedDocumentRepository;
		this.securityService = securityService;
	}

	@Transactional(readOnly = true)
	public SessionReportResponse getSessionReport(Long sessionId) {
		DefenseSession session = defenseSessionRepository.findById(sessionId)
				.orElseThrow(() -> new EntityNotFoundException("Session non trouvée"));

		List<Group> groups = groupRepository.findByDefenseSessionId(sessionId);
		List<Long> projectIds = groups.stream().filter(g -> g.getProject() != null).map(g -> g.getProject().getId())
				.distinct().toList();

		List<DefenseReportDetails> details = new ArrayList<>();
		int passed = 0;
		int total = 0;

		for (Defense defense : defenseRepository.findAllWithMembers()) {
			if (defense.getProject() == null || !projectIds.contains(defense.getProject().getId()))
				continue;
			if (defense.getStatus() == DefenseStatus.CANCELLED)
				continue;

			total++;
			Project project = defense.getProject();
			List<String> studentNames = resolveStudentNames(project);
			List<String> juryNames = defense.getMembers().stream().map(
					m -> m.getRoleName() + ": " + m.getTeacher().getFirstName() + " " + m.getTeacher().getLastName())
					.toList();
			String roomName = defense.getRoom() != null ? defense.getRoom().getName() : null;

			if (defense.getFinalScore() != null && defense.getFinalScore() >= 10) {
				passed++;
			}

			details.add(new DefenseReportDetails(project.getTitle(), studentNames,
					defense.getDate() != null ? defense.getDate().toString() : null,
					defense.getTime() != null ? defense.getTime().toString() : null, roomName, juryNames,
					defense.getFinalScore(), defense.getMention()));
		}

		double passRate = total > 0 ? (double) passed / total * 100 : 0;

		return new SessionReportResponse(session.getName(),
				session.getDefenseType() != null ? session.getDefenseType().name() : null,
				session.getStartDate() != null ? session.getStartDate().toString() : null,
				session.getEndDate() != null ? session.getEndDate().toString() : null, total, passed, passRate,
				details);
	}

	@Transactional(readOnly = true)
	public List<TeacherWorkloadResponse> getTeacherWorkload() {
		List<Teacher> teachers = teacherRepository.findAll();
		List<Defense> allDefenses = defenseRepository.findAllWithMembers();
		List<Project> allProjects = projectRepository.findAll();

		List<TeacherWorkloadResponse> workloads = new ArrayList<>();
		for (Teacher teacher : teachers) {
			List<String> supervisedProjects = allProjects.stream()
					.filter(p -> p.getSupervisor() != null && p.getSupervisor().getId().equals(teacher.getId()))
					.map(Project::getTitle).toList();

			List<String> juryDefenses = allDefenses.stream()
					.filter(d -> d.getMembers().stream().anyMatch(m -> m.getTeacher().getId().equals(teacher.getId())))
					.map(d -> d.getProject() != null ? d.getProject().getTitle() : "N/A").toList();

			int total = supervisedProjects.size() + juryDefenses.size();

			workloads.add(
					new TeacherWorkloadResponse(teacher.getId(), teacher.getFirstName() + " " + teacher.getLastName(),
							supervisedProjects.size(), supervisedProjects, juryDefenses.size(), juryDefenses, total));
		}

		workloads.sort(Comparator.comparingInt(TeacherWorkloadResponse::totalWorkload).reversed());
		return workloads;
	}

	@Transactional(readOnly = true)
	public List<GradeHistoryResponse> getGradeHistory(Long sessionId) {
		List<Group> groups = groupRepository.findByDefenseSessionId(sessionId);
		List<Long> projectIds = groups.stream().filter(g -> g.getProject() != null).map(g -> g.getProject().getId())
				.distinct().toList();

		List<Project> projects = projectRepository.findAllById(projectIds);
		List<Defense> defenses = defenseRepository.findAllWithMembers();
		List<Evaluation> allEvaluations = evaluationRepository.findAll();

		Map<Long, List<Evaluation>> evalsByProject = allEvaluations.stream()
				.filter(e -> e.getDefense() != null && e.getDefense().getProject() != null)
				.collect(Collectors.groupingBy(e -> e.getDefense().getProject().getId()));

		List<GradeHistoryResponse> result = new ArrayList<>();
		for (Project project : projects) {
			List<Evaluation> projectEvals = evalsByProject.getOrDefault(project.getId(), List.of());

			List<EvaluationEntry> entries = projectEvals.stream()
					.filter(e -> e.getStatus() == EvaluationStatus.SUBMITTED)
					.map(e -> new EvaluationEntry(
							e.getTeacherId() != null ? resolveTeacherName(e.getTeacherId()) : "Inconnu", "Jury",
							e.getScore(), e.getSubmittedAt()))
					.toList();

			Double computedAverage = entries.stream().filter(e -> e.score() != null).mapToDouble(EvaluationEntry::score)
					.average().orElse(0);

			Defense defense = defenses.stream()
					.filter(d -> d.getProject() != null && d.getProject().getId().equals(project.getId())).findFirst()
					.orElse(null);

			result.add(new GradeHistoryResponse(project.getId(), project.getTitle(), entries,
					entries.isEmpty() ? null : Math.round(computedAverage * 100.0) / 100.0,
					defense != null ? defense.getFinalScore() : null,
					defense != null ? defense.getDeliberationComment() : null, null, null));
		}

		return result;
	}

	@Transactional
	public void recordDocumentGeneration(String type, Long sessionId, Long fileSize) {
		GeneratedDocument doc = new GeneratedDocument();
		doc.setType(type);
		doc.setGeneratedBy(securityService.getCurrentUserId());
		doc.setGeneratedAt(LocalDateTime.now());
		doc.setSessionId(sessionId);
		doc.setFileSize(fileSize);
		generatedDocumentRepository.save(doc);
	}

	private List<String> resolveStudentNames(Project project) {
		List<Group> groups = groupRepository.findByProjectId(project.getId());
		for (Group g : groups) {
			if (g.getStudents() != null && !g.getStudents().isEmpty()) {
				return g.getStudents().stream().map(s -> s.getFirstName() + " " + s.getLastName()).toList();
			}
		}
		return List.of();
	}

	private String resolveTeacherName(Long teacherId) {
		return teacherRepository.findById(teacherId).map(t -> t.getFirstName() + " " + t.getLastName())
				.orElse("Inconnu");
	}
}
