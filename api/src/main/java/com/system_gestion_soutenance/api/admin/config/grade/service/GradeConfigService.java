package com.system_gestion_soutenance.api.admin.config.grade.service;

import com.system_gestion_soutenance.api.admin.config.grade.dto.CreateGradeRequest;
import com.system_gestion_soutenance.api.admin.config.grade.entity.Grade;
import com.system_gestion_soutenance.api.admin.config.grade.repository.GradeRepository;
import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.common.service.BaseCrudService;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GradeConfigService extends BaseCrudService<Grade, Long, CreateGradeRequest> {

	private final GradeRepository gradeRepository;
	private final TeacherRepository teacherRepository;

	public GradeConfigService(GradeRepository gradeRepository, TeacherRepository teacherRepository) {
		super(gradeRepository);
		this.gradeRepository = gradeRepository;
		this.teacherRepository = teacherRepository;
	}

	@Audited(action = "CREATE", entity = "Grade")
	@Transactional
	public Grade create(CreateGradeRequest request) {
		if (gradeRepository.findByName(request.name()).isPresent()) {
			throw new InvalidBusinessStateException("Un grade avec ce nom existe déjà");
		}

		Grade grade = new Grade();
		grade.setName(request.name());
		return save(grade);
	}

	@Audited(action = "UPDATE", entity = "Grade")
	@Transactional
	public Grade update(Long id, CreateGradeRequest request) {
		Grade grade = findByIdOrThrow(id, "Grade");
		grade.setName(request.name());
		return save(grade);
	}

	@Audited(action = "DELETE", entity = "Grade")
	@Transactional
	public void delete(Long id) {
		deleteWithCheck(id, "Grade", () -> !teacherRepository.findByGradeId(id).isEmpty());
	}
}
