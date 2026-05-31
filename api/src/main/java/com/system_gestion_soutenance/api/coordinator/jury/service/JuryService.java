package com.system_gestion_soutenance.api.coordinator.jury.service;

import com.system_gestion_soutenance.api.admin.config.juryrole.entity.JuryRoleTemplate;
import com.system_gestion_soutenance.api.admin.config.juryrole.repository.JuryRoleTemplateRepository;
import com.system_gestion_soutenance.api.coordinator.jury.dto.CreateJuryRequest;
import com.system_gestion_soutenance.api.coordinator.jury.entity.Jury;
import com.system_gestion_soutenance.api.coordinator.jury.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class JuryService {

	private final JuryRepository juryRepository;
	private final ProjectRepository projectRepository;
	private final TeacherRepository teacherRepository;
	private final JuryRoleTemplateRepository juryRoleTemplateRepository;

	public JuryService(JuryRepository juryRepository, ProjectRepository projectRepository,
			TeacherRepository teacherRepository, JuryRoleTemplateRepository juryRoleTemplateRepository) {
		this.juryRepository = juryRepository;
		this.projectRepository = projectRepository;
		this.teacherRepository = teacherRepository;
		this.juryRoleTemplateRepository = juryRoleTemplateRepository;
	}

	public List<Map<String, Object>> findAll() {
		return juryRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
	}

	@Transactional
	public Map<String, Object> create(CreateJuryRequest request) {
		Project project = projectRepository.findById(request.projectId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Projet introuvable"));

		JuryRoleTemplate template = juryRoleTemplateRepository.findById(request.templateId()).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Template de rôle jury introuvable"));

		validateNoDuplicateTeachers(request.members());

		Jury jury = new Jury();
		jury.setProject(project);
		jury.setTemplate(template);

		List<JuryMember> members = request.members().stream().map(m -> {
			Teacher teacher = teacherRepository.findById(m.teacherId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
							"Enseignant introuvable: " + m.teacherId()));
			JuryMember jm = new JuryMember();
			jm.setJury(jury);
			jm.setRoleName(m.roleName());
			jm.setTeacher(teacher);
			return jm;
		}).collect(Collectors.toList());
		jury.setMembers(members);

		return toResponse(juryRepository.save(jury));
	}

	@Transactional
	public Map<String, Object> update(Long id, Map<String, Object> updates) {
		Jury jury = juryRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jury non trouvé"));

		if (updates.containsKey("projectId")) {
			Project project = projectRepository.findById(Long.parseLong((String) updates.get("projectId")))
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Projet introuvable"));
			jury.setProject(project);
		}
		if (updates.containsKey("templateId")) {
			JuryRoleTemplate template = juryRoleTemplateRepository
					.findById(Long.parseLong((String) updates.get("templateId")))
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
							"Template de rôle jury introuvable"));
			jury.setTemplate(template);
		}
		if (updates.containsKey("members")) {
			@SuppressWarnings("unchecked")
			List<Map<String, String>> memberData = (List<Map<String, String>>) updates.get("members");
			validateNoDuplicateTeachers(memberData);

			jury.getMembers().clear();
			for (Map<String, String> m : memberData) {
				Teacher teacher = teacherRepository.findById(Long.parseLong(m.get("teacherId")))
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
								"Enseignant introuvable: " + m.get("teacherId")));
				JuryMember jm = new JuryMember();
				jm.setJury(jury);
				jm.setRoleName(m.get("roleName"));
				jm.setTeacher(teacher);
				jury.getMembers().add(jm);
			}
		}

		return toResponse(juryRepository.save(jury));
	}

	public void delete(Long id) {
		if (!juryRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Jury non trouvé");
		}
		juryRepository.deleteById(id);
	}

	private Map<String, Object> toResponse(Jury jury) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("id", jury.getId());
		map.put("projectId", jury.getProject().getId());
		map.put("projectTitle", jury.getProject().getTitle());
		map.put("defenseType", jury.getProject().getDefenseType());
		map.put("templateId", jury.getTemplateId());
		map.put("templateName", jury.getTemplateName());

		List<Map<String, Object>> members = jury.getMembers().stream().map(m -> {
			Map<String, Object> mm = new LinkedHashMap<>();
			mm.put("roleName", m.getRoleName());
			mm.put("teacherId", m.getTeacher().getId());
			mm.put("teacherName", m.getTeacher().getFirstName() + " " + m.getTeacher().getLastName());
			return mm;
		}).collect(Collectors.toList());
		map.put("members", members);

		return map;
	}

	private void validateNoDuplicateTeachers(List<?> members) {
		Set<Long> teacherIds = new HashSet<>();
		for (Object m : members) {
			Long tid;
			if (m instanceof CreateJuryRequest.MemberEntry entry) {
				tid = entry.teacherId();
			} else if (m instanceof Map<?, ?> map) {
				tid = Long.parseLong((String) map.get("teacherId"));
			} else {
				continue;
			}
			if (!teacherIds.add(tid)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Un enseignant ne peut être assigné qu'à un seul rôle dans un même jury");
			}
		}
	}
}
