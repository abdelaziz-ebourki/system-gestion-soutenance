package com.system_gestion_soutenance.api.admin.config.grade.service;

import com.system_gestion_soutenance.api.admin.config.grade.dto.CreateGradeRequest;
import com.system_gestion_soutenance.api.admin.config.grade.entity.Grade;
import com.system_gestion_soutenance.api.admin.config.grade.repository.GradeRepository;
import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class GradeConfigService {

	private final GradeRepository gradeRepository;
	private final TeacherRepository teacherRepository;

	public GradeConfigService(GradeRepository gradeRepository, TeacherRepository teacherRepository) {
		this.gradeRepository = gradeRepository;
		this.teacherRepository = teacherRepository;
	}

	public List<Grade> findAll() {
		return gradeRepository.findAll();
	}

	@Audited(action = "CREATE", entity = "Grade")
	@Transactional
	public Grade create(CreateGradeRequest request) {
		if (gradeRepository.findByName(request.name()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un grade avec ce nom existe déjà");
		}

		Grade grade = new Grade();
		grade.setName(request.name());
		return gradeRepository.save(grade);
	}

	@Audited(action = "UPDATE", entity = "Grade")
	@Transactional
	public Grade update(Long id, CreateGradeRequest request) {
		Grade grade = gradeRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grade non trouvé"));

		grade.setName(request.name());
		return gradeRepository.save(grade);
	}

	@Audited(action = "DELETE", entity = "Grade")
	@Transactional
	public void delete(Long id) {
		Grade grade = gradeRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grade non trouvé"));

		if (!teacherRepository.findByGradeId(id).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Impossible de supprimer ce grade car des enseignants y sont rattachés");
		}

		gradeRepository.delete(grade);
	}
}
