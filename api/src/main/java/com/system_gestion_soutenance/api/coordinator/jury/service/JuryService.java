package com.system_gestion_soutenance.api.coordinator.jury.service;

import com.system_gestion_soutenance.api.admin.config.juryrole.entity.JuryRoleTemplate;
import com.system_gestion_soutenance.api.admin.config.juryrole.repository.JuryRoleTemplateRepository;
import com.system_gestion_soutenance.api.coordinator.jury.dto.CreateJuryRequest;
import com.system_gestion_soutenance.api.coordinator.jury.dto.JuryResponse;
import com.system_gestion_soutenance.api.coordinator.jury.dto.UpdateJuryRequest;
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

	@Transactional(readOnly = true)
	public List<JuryResponse> findAll() {
		return juryRepository.findAllWithDetails().stream().map(this::toResponse).collect(Collectors.toList());
	}

	@Transactional
	public JuryResponse create(CreateJuryRequest request) {
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
	public JuryResponse update(Long id, UpdateJuryRequest updates) {
		Jury jury = juryRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jury non trouvé"));

		if (updates.projectId() != null) {
			Project project = projectRepository.findById(updates.projectId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Projet introuvable"));
			jury.setProject(project);
		}
		if (updates.templateId() != null) {
			JuryRoleTemplate template = juryRoleTemplateRepository.findById(updates.templateId()).orElseThrow(
					() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Template de rôle jury introuvable"));
			jury.setTemplate(template);
		}
		if (updates.members() != null) {
			validateNoDuplicateTeachers(updates.members());

			jury.getMembers().clear();
			for (UpdateJuryRequest.MemberEntry m : updates.members()) {
				Teacher teacher = teacherRepository.findById(m.teacherId())
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
								"Enseignant introuvable: " + m.teacherId()));
				JuryMember jm = new JuryMember();
				jm.setJury(jury);
				jm.setRoleName(m.roleName());
				jm.setTeacher(teacher);
				jury.getMembers().add(jm);
			}
		}

		return toResponse(juryRepository.save(jury));
	}

	@Transactional
	public void delete(Long id) {
		if (!juryRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Jury non trouvé");
		}
		juryRepository.deleteById(id);
	}

	private JuryResponse toResponse(Jury jury) {
		List<JuryResponse.MemberResponse> members = jury.getMembers().stream()
				.map(m -> new JuryResponse.MemberResponse(m.getRoleName(), m.getTeacher().getId(),
						m.getTeacher().getFirstName() + " " + m.getTeacher().getLastName()))
				.collect(Collectors.toList());

		return new JuryResponse(jury.getId(), jury.getProject().getId(), jury.getProject().getTitle(),
				jury.getProject().getDefenseType(), jury.getTemplateId(), jury.getTemplateName(), members);
	}

	private void validateNoDuplicateTeachers(List<?> members) {
		Set<Long> teacherIds = new HashSet<>();
		for (Object m : members) {
			Long tid;
			if (m instanceof CreateJuryRequest.MemberEntry entry) {
				tid = entry.teacherId();
			} else if (m instanceof UpdateJuryRequest.MemberEntry entry) {
				tid = entry.teacherId();
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
