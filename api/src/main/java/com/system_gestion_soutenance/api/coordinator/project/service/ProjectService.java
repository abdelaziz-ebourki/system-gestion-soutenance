package com.system_gestion_soutenance.api.coordinator.project.service;

import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.project.dto.CreateProjectRequest;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.coordinator.schedule.repository.SlotAssignmentRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProjectService {

	private final ProjectRepository projectRepository;
	private final TeacherRepository teacherRepository;
	private final StudentRepository studentRepository;
	private final GroupRepository groupRepository;
	private final JuryRepository juryRepository;
	private final SlotAssignmentRepository slotAssignmentRepository;

	public ProjectService(ProjectRepository projectRepository, TeacherRepository teacherRepository,
			StudentRepository studentRepository, GroupRepository groupRepository, JuryRepository juryRepository,
			SlotAssignmentRepository slotAssignmentRepository) {
		this.projectRepository = projectRepository;
		this.teacherRepository = teacherRepository;
		this.studentRepository = studentRepository;
		this.groupRepository = groupRepository;
		this.juryRepository = juryRepository;
		this.slotAssignmentRepository = slotAssignmentRepository;
	}

	public List<Map<String, Object>> findAll() {
		return projectRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
	}

	public Map<String, Object> create(CreateProjectRequest request) {
		Teacher supervisor = teacherRepository.findById(request.supervisorId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Encadrant introuvable"));

		List<Student> students = Collections.emptyList();
		if (request.studentIds() != null) {
			students = studentRepository.findAllById(request.studentIds());
		}

		Project project = new Project();
		project.setTitle(request.title());
		project.setDescription(request.description());
		project.setDefenseType(request.defenseType());
		project.setStatus("pending");
		project.setSupervisor(supervisor);
		project.setStudents(students);

		return toResponse(projectRepository.save(project));
	}

	public Map<String, Object> update(Long id, Map<String, Object> updates) {
		Project project = projectRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projet non trouvé"));

		if (updates.containsKey("title"))
			project.setTitle((String) updates.get("title"));
		if (updates.containsKey("description"))
			project.setDescription((String) updates.get("description"));
		if (updates.containsKey("defenseType"))
			project.setDefenseType((String) updates.get("defenseType"));
		if (updates.containsKey("status"))
			project.setStatus((String) updates.get("status"));
		if (updates.containsKey("supervisorId")) {
			Teacher supervisor = teacherRepository.findById(Long.parseLong((String) updates.get("supervisorId")))
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Encadrant introuvable"));
			project.setSupervisor(supervisor);
		}
		if (updates.containsKey("studentIds")) {
			@SuppressWarnings("unchecked")
			List<Object> rawIds = (List<Object>) updates.get("studentIds");
			List<Long> ids = rawIds.stream()
					.map(v -> v instanceof Number ? ((Number) v).longValue() : Long.parseLong(v.toString()))
					.collect(Collectors.toList());
			project.setStudents(studentRepository.findAllById(ids));
		}

		return toResponse(projectRepository.save(project));
	}

	public void delete(Long id) {
		Project project = projectRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projet non trouvé"));

		if (!juryRepository.findByProjectId(id).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Impossible de supprimer ce projet car des jurys y sont rattachés");
		}
		if (!groupRepository.findByProjectId(id).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Impossible de supprimer ce projet car des groupes y sont rattachés");
		}
		if (slotAssignmentRepository.findAll().stream().anyMatch(s -> project.getId().equals(s.getProjectId()))) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Impossible de supprimer ce projet car des soutenances sont planifiées");
		}

		projectRepository.delete(project);
	}

	private Map<String, Object> toResponse(Project project) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("id", project.getId());
		map.put("title", project.getTitle());
		map.put("description", project.getDescription());

		List<String> studentIds = project.getStudents() != null
				? project.getStudents().stream().map(s -> String.valueOf(s.getId())).collect(Collectors.toList())
				: List.of();
		List<String> studentNames = project.getStudents() != null
				? project.getStudents().stream().map(s -> s.getFirstName() + " " + s.getLastName())
						.collect(Collectors.toList())
				: List.of();
		map.put("studentIds", studentIds);
		map.put("studentNames", studentNames);

		map.put("supervisorId", project.getSupervisor() != null ? project.getSupervisor().getId() : null);
		map.put("supervisorName",
				project.getSupervisor() != null
						? project.getSupervisor().getFirstName() + " " + project.getSupervisor().getLastName()
						: null);
		map.put("defenseType", project.getDefenseType());
		map.put("status", project.getStatus());
		return map;
	}
}
