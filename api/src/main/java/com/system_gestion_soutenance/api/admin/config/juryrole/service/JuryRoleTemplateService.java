package com.system_gestion_soutenance.api.admin.config.juryrole.service;

import com.system_gestion_soutenance.api.admin.config.juryrole.dto.CreateJuryRoleTemplateRequest;
import com.system_gestion_soutenance.api.admin.config.juryrole.entity.JuryRoleTemplate;
import com.system_gestion_soutenance.api.admin.config.juryrole.entity.TemplateRole;
import com.system_gestion_soutenance.api.admin.config.juryrole.repository.JuryRoleTemplateRepository;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseType;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.common.audit.Audited;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class JuryRoleTemplateService {

	private final JuryRoleTemplateRepository juryRoleTemplateRepository;
	private final DefenseSessionRepository defenseSessionRepository;

	public JuryRoleTemplateService(JuryRoleTemplateRepository juryRoleTemplateRepository,
			DefenseSessionRepository defenseSessionRepository) {
		this.juryRoleTemplateRepository = juryRoleTemplateRepository;
		this.defenseSessionRepository = defenseSessionRepository;
	}

	public List<JuryRoleTemplate> findAll() {
		return juryRoleTemplateRepository.findAll();
	}

	@Transactional
	@Audited(action = "CREATE", entity = "JuryRoleTemplate")
	public JuryRoleTemplate create(CreateJuryRoleTemplateRequest request) {
		if (juryRoleTemplateRepository.findByName(request.name()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un template avec ce nom existe déjà");
		}

		validateRoleNames(request.roles());

		JuryRoleTemplate template = new JuryRoleTemplate();
		template.setName(request.name());
		template.setDefenseType(DefenseType.valueOf(request.defenseType().toUpperCase()));
		template.setRoles(request.roles().stream().map(r -> new TemplateRole(r.name(), r.count(), r.coefficient()))
				.collect(Collectors.toList()));
		return juryRoleTemplateRepository.save(template);
	}

	@Transactional
	@Audited(action = "UPDATE", entity = "JuryRoleTemplate")
	public JuryRoleTemplate update(Long id, CreateJuryRoleTemplateRequest request) {
		JuryRoleTemplate template = juryRoleTemplateRepository.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template de rôle jury non trouvé"));

		validateRoleNames(request.roles());

		template.setName(request.name());
		template.setDefenseType(DefenseType.valueOf(request.defenseType().toUpperCase()));
		template.getRoles().clear();
		template.getRoles().addAll(request.roles().stream()
				.map(r -> new TemplateRole(r.name(), r.count(), r.coefficient())).collect(Collectors.toList()));
		return juryRoleTemplateRepository.save(template);
	}

	@Transactional
	@Audited(action = "DELETE", entity = "JuryRoleTemplate")
	public void delete(Long id) {
		JuryRoleTemplate template = juryRoleTemplateRepository.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template de rôle jury non trouvé"));

		if (!defenseSessionRepository.findByJuryRoleTemplate_Id(id).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Impossible de supprimer ce template car des sessions de soutenance l'utilisent");
		}

		juryRoleTemplateRepository.delete(template);
	}

	private void validateRoleNames(List<CreateJuryRoleTemplateRequest.RoleEntry> roles) {
		Set<String> names = roles.stream().map(CreateJuryRoleTemplateRequest.RoleEntry::name)
				.collect(Collectors.toSet());
		if (names.size() != roles.size()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Les noms de rôles doivent être uniques dans le template");
		}
	}
}
