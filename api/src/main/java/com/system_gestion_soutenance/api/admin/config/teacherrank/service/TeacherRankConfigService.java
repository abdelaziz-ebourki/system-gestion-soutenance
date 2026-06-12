package com.system_gestion_soutenance.api.admin.config.teacherrank.service;

import com.system_gestion_soutenance.api.admin.config.teacherrank.dto.CreateTeacherRankRequest;
import com.system_gestion_soutenance.api.admin.config.teacherrank.entity.TeacherRank;
import com.system_gestion_soutenance.api.admin.config.teacherrank.repository.TeacherRankRepository;
import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.common.service.BaseCrudService;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@SuppressWarnings("PMD")

@Service
@Transactional(readOnly = true)
public class TeacherRankConfigService extends BaseCrudService<TeacherRank, Long, CreateTeacherRankRequest> {

	private final TeacherRankRepository teacherRankRepository;
	private final TeacherRepository teacherRepository;

	public TeacherRankConfigService(TeacherRankRepository teacherRankRepository, TeacherRepository teacherRepository) {
		super(teacherRankRepository);
		this.teacherRankRepository = teacherRankRepository;
		this.teacherRepository = teacherRepository;
	}

	@Audited(action = "CREATE", entity = "TeacherRank")
	@Transactional
	public TeacherRank create(CreateTeacherRankRequest request) {
		if (teacherRankRepository.findByName(request.name()).isPresent()) {
			throw new InvalidBusinessStateException("Un rank avec ce nom existe déjà");
		}

		TeacherRank teacherRank = new TeacherRank();
		teacherRank.setName(request.name());
		return save(teacherRank);
	}

	@Audited(action = "UPDATE", entity = "TeacherRank")
	@Transactional
	public TeacherRank update(Long id, CreateTeacherRankRequest request) {
		TeacherRank teacherRank = findByIdOrThrow(id, "TeacherRank");
		teacherRank.setName(request.name());
		return save(teacherRank);
	}

	@Audited(action = "DELETE", entity = "TeacherRank")
	@Transactional
	public void delete(Long id) {
		deleteWithCheck(id, "TeacherRank", () -> !teacherRepository.findByTeacherRankId(id).isEmpty());
	}
}
