package com.system_gestion_soutenance.api.admin.config.level.service;

import com.system_gestion_soutenance.api.admin.config.level.dto.CreateLevelRequest;
import com.system_gestion_soutenance.api.admin.config.level.dto.UpdateLevelRequest;
import com.system_gestion_soutenance.api.admin.config.level.entity.Level;
import com.system_gestion_soutenance.api.admin.config.level.repository.LevelRepository;
import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.service.AbstractNamedConfigService;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@SuppressWarnings("PMD")

@Service
@Transactional(readOnly = true)
public class LevelConfigService extends AbstractNamedConfigService<Level, CreateLevelRequest, UpdateLevelRequest> {

	private final LevelRepository levelRepository;
	private final StudentRepository studentRepository;

	public LevelConfigService(LevelRepository levelRepository, StudentRepository studentRepository) {
		super(levelRepository);
		this.levelRepository = levelRepository;
		this.studentRepository = studentRepository;
	}

	public PaginatedResponse<Level> findAll(int page, int limit) {
		return findAllPaged(page, limit);
	}

	@Audited(action = "CREATE", entity = "Level")
	@Transactional
	public Level create(CreateLevelRequest request) {
		return createNamed(request);
	}

	@Audited(action = "UPDATE", entity = "Level")
	@Transactional
	public Level update(Long id, CreateLevelRequest request) {
		return updateNamed(id, request);
	}

	@Audited(action = "UPDATE", entity = "Level")
	@Transactional
	public Level updatePartial(Long id, UpdateLevelRequest request) {
		return updatePartialNamed(id, request);
	}

	@Audited(action = "DELETE", entity = "Level")
	@Transactional
	public void delete(Long id) {
		deleteNamed(id);
	}

	@Override
	protected Level newEntity() {
		return new Level();
	}

	@Override
	protected void setName(Level entity, String name) {
		entity.setName(name);
	}

	@Override
	protected Optional<Level> findByName(String name) {
		return levelRepository.findByName(name);
	}

	@Override
	protected String duplicateMessage() {
		return "Un niveau avec ce nom existe déjà";
	}

	@Override
	protected String entityLabel() {
		return "Niveau";
	}

	@Override
	protected Supplier<Boolean> usageCheck(Long id) {
		return () -> !studentRepository.findByLevelId(id).isEmpty();
	}
}
