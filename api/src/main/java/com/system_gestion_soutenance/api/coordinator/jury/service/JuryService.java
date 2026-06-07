package com.system_gestion_soutenance.api.coordinator.jury.service;

import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.admin.config.juryrole.entity.JuryRoleTemplate;
import com.system_gestion_soutenance.api.admin.config.juryrole.repository.JuryRoleTemplateRepository;
import com.system_gestion_soutenance.api.coordinator.jury.dto.CreateJuryRequest;
import com.system_gestion_soutenance.api.coordinator.jury.dto.UpdateJuryRequest;
import com.system_gestion_soutenance.api.coordinator.jury.entity.Jury;
import com.system_gestion_soutenance.api.coordinator.jury.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	public List<Jury> findAll() {
		return juryRepository.findAllWithDetails();
	}

	@Audited(action = "CREATE", entity = "Jury")
	@Transactional
	public Jury create(CreateJuryRequest request) {
		Project project = projectRepository.findById(request.projectId())
				.orElseThrow(() -> new InvalidBusinessStateException("Projet introuvable"));

		JuryRoleTemplate template = juryRoleTemplateRepository.findById(request.templateId())
				.orElseThrow(() -> new InvalidBusinessStateException("Template de rôle jury introuvable"));

		validateNoDuplicateTeachers(request.members());

		Jury jury = new Jury();
		jury.setProject(project);
		jury.setTemplate(template);

		List<JuryMember> members = request.members().stream().map(m -> {
			Teacher teacher = teacherRepository.findById(m.teacherId())
					.orElseThrow(() -> new InvalidBusinessStateException("Enseignant introuvable: " + m.teacherId()));
			JuryMember jm = new JuryMember();
			jm.setJury(jury);
			jm.setRoleName(m.roleName());
			jm.setTeacher(teacher);
			return jm;
		}).toList();
		jury.setMembers(members);

		return juryRepository.save(jury);
	}

	@Audited(action = "UPDATE", entity = "Jury")
	@Transactional
	public Jury update(Long id, UpdateJuryRequest updates) {
		Jury jury = juryRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Jury non trouvé"));

		if (updates.projectId() != null) {
			Project project = projectRepository.findById(updates.projectId())
					.orElseThrow(() -> new InvalidBusinessStateException("Projet introuvable"));
			jury.setProject(project);
		}
		if (updates.templateId() != null) {
			JuryRoleTemplate template = juryRoleTemplateRepository.findById(updates.templateId())
					.orElseThrow(() -> new InvalidBusinessStateException("Template de rôle jury introuvable"));
			jury.setTemplate(template);
		}
		if (updates.members() != null) {
			validateNoDuplicateTeachers(updates.members());

			jury.getMembers().clear();
			for (UpdateJuryRequest.MemberEntry m : updates.members()) {
				Teacher teacher = teacherRepository.findById(m.teacherId()).orElseThrow(
						() -> new InvalidBusinessStateException("Enseignant introuvable: " + m.teacherId()));
				JuryMember jm = new JuryMember();
				jm.setJury(jury);
				jm.setRoleName(m.roleName());
				jm.setTeacher(teacher);
				jury.getMembers().add(jm);
			}
		}

		return juryRepository.save(jury);
	}

	@Audited(action = "DELETE", entity = "Jury")
	@Transactional
	public void delete(Long id) {
		if (!juryRepository.existsById(id)) {
			throw new EntityNotFoundException("Jury non trouvé");
		}
		juryRepository.deleteById(id);
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
				throw new InvalidBusinessStateException(
						"Un enseignant ne peut être assigné qu'à un seul rôle dans un même jury");
			}
		}
	}
}
