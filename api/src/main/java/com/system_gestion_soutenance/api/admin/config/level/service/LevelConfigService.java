package com.system_gestion_soutenance.api.admin.config.level.service;

import com.system_gestion_soutenance.api.admin.config.level.dto.CreateLevelRequest;
import com.system_gestion_soutenance.api.admin.config.level.entity.Level;
import com.system_gestion_soutenance.api.admin.config.level.repository.LevelRepository;
import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class LevelConfigService {

	private final LevelRepository levelRepository;
	private final StudentRepository studentRepository;

	public LevelConfigService(LevelRepository levelRepository, StudentRepository studentRepository) {
		this.levelRepository = levelRepository;
		this.studentRepository = studentRepository;
	}

	public List<Level> findAll() {
		return levelRepository.findAll();
	}

	@Audited(action = "CREATE", entity = "Level")
	@Transactional
	public Level create(CreateLevelRequest request) {
		if (levelRepository.findByName(request.name()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un niveau avec ce nom existe déjà");
		}

		Level level = new Level();
		level.setName(request.name());
		return levelRepository.save(level);
	}

	@Audited(action = "UPDATE", entity = "Level")
	@Transactional
	public Level update(Long id, CreateLevelRequest request) {
		Level level = levelRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Niveau non trouvé"));

		level.setName(request.name());
		return levelRepository.save(level);
	}

	@Audited(action = "DELETE", entity = "Level")
	@Transactional
	public void delete(Long id) {
		Level level = levelRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Niveau non trouvé"));

		if (!studentRepository.findByLevelId(id).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Impossible de supprimer ce niveau car des étudiants y sont inscrits");
		}

		levelRepository.delete(level);
	}
}
