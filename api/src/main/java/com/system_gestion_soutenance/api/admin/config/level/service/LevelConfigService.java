package com.system_gestion_soutenance.api.admin.config.level.service;

import com.system_gestion_soutenance.api.admin.config.level.dto.CreateLevelRequest;
import com.system_gestion_soutenance.api.admin.config.level.entity.Level;
import com.system_gestion_soutenance.api.admin.config.level.repository.LevelRepository;
import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.common.service.BaseCrudService;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@SuppressWarnings("PMD")

@Service
@Transactional(readOnly = true)
public class LevelConfigService extends BaseCrudService<Level, Long, CreateLevelRequest> {

	private final LevelRepository levelRepository;
	private final StudentRepository studentRepository;

	public LevelConfigService(LevelRepository levelRepository, StudentRepository studentRepository) {
		super(levelRepository);
		this.levelRepository = levelRepository;
		this.studentRepository = studentRepository;
	}

	@Audited(action = "CREATE", entity = "Level")
	@Transactional
	public Level create(CreateLevelRequest request) {
		if (levelRepository.findByName(request.name()).isPresent()) {
			throw new InvalidBusinessStateException("Un niveau avec ce nom existe déjà");
		}

		Level level = new Level();
		level.setName(request.name());
		return save(level);
	}

	@Audited(action = "UPDATE", entity = "Level")
	@Transactional
	public Level update(Long id, CreateLevelRequest request) {
		Level level = findByIdOrThrow(id, "Niveau");
		level.setName(request.name());
		return save(level);
	}

	@Audited(action = "DELETE", entity = "Level")
	@Transactional
	public void delete(Long id) {
		deleteWithCheck(id, "Niveau", () -> !studentRepository.findByLevelId(id).isEmpty());
	}
}