package com.system_gestion_soutenance.api.admin.config.teacherrank.service;

import com.system_gestion_soutenance.api.admin.config.teacherrank.dto.CreateTeacherRankRequest;
import com.system_gestion_soutenance.api.admin.config.teacherrank.dto.UpdateTeacherRankRequest;
import com.system_gestion_soutenance.api.admin.config.teacherrank.entity.TeacherRank;
import com.system_gestion_soutenance.api.admin.config.teacherrank.repository.TeacherRankRepository;
import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.service.BaseCrudService;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

	public PaginatedResponse<TeacherRank> findAll(int page, int limit) {
		Page<TeacherRank> rankPage = teacherRankRepository.findAll(PageRequest.of(page, limit));
		return new PaginatedResponse<>(rankPage.getContent(), rankPage.getTotalElements(), rankPage.getTotalPages(),
				page, limit);
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

	@Audited(action = "UPDATE", entity = "TeacherRank")
	@Transactional
	public TeacherRank updatePartial(Long id, UpdateTeacherRankRequest request) {
		TeacherRank teacherRank = findByIdOrThrow(id, "TeacherRank");
		if (request.name() != null) {
			teacherRank.setName(request.name());
		}
		return save(teacherRank);
	}

	@Audited(action = "DELETE", entity = "TeacherRank")
	@Transactional
	public void delete(Long id) {
		deleteWithCheck(id, "TeacherRank", () -> !teacherRepository.findByTeacherRankId(id).isEmpty());
	}
}
