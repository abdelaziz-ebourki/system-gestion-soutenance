package com.system_gestion_soutenance.api.admin.config.teacherrank.service;

import com.system_gestion_soutenance.api.admin.config.teacherrank.dto.CreateTeacherRankRequest;
import com.system_gestion_soutenance.api.admin.config.teacherrank.dto.UpdateTeacherRankRequest;
import com.system_gestion_soutenance.api.admin.config.teacherrank.entity.TeacherRank;
import com.system_gestion_soutenance.api.admin.config.teacherrank.repository.TeacherRankRepository;
import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.service.AbstractNamedConfigService;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@SuppressWarnings("PMD")

@Service
@Transactional(readOnly = true)
public class TeacherRankConfigService
		extends AbstractNamedConfigService<TeacherRank, CreateTeacherRankRequest, UpdateTeacherRankRequest> {

	private final TeacherRankRepository teacherRankRepository;
	private final TeacherRepository teacherRepository;

	public TeacherRankConfigService(TeacherRankRepository teacherRankRepository, TeacherRepository teacherRepository) {
		super(teacherRankRepository);
		this.teacherRankRepository = teacherRankRepository;
		this.teacherRepository = teacherRepository;
	}

	public PaginatedResponse<TeacherRank> findAll(int page, int limit) {
		return findAllPaged(page, limit);
	}

	@Audited(action = "CREATE", entity = "TeacherRank")
	@Transactional
	public TeacherRank create(CreateTeacherRankRequest request) {
		return createNamed(request);
	}

	@Audited(action = "UPDATE", entity = "TeacherRank")
	@Transactional
	public TeacherRank update(Long id, CreateTeacherRankRequest request) {
		return updateNamed(id, request);
	}

	@Audited(action = "UPDATE", entity = "TeacherRank")
	@Transactional
	public TeacherRank updatePartial(Long id, UpdateTeacherRankRequest request) {
		return updatePartialNamed(id, request);
	}

	@Audited(action = "DELETE", entity = "TeacherRank")
	@Transactional
	public void delete(Long id) {
		deleteNamed(id);
	}

	@Override
	protected TeacherRank newEntity() {
		return new TeacherRank();
	}

	@Override
	protected void setName(TeacherRank entity, String name) {
		entity.setName(name);
	}

	@Override
	protected Optional<TeacherRank> findByName(String name) {
		return teacherRankRepository.findByName(name);
	}

	@Override
	protected String duplicateMessage() {
		return "Un rank avec ce nom existe déjà";
	}

	@Override
	protected String entityLabel() {
		return "TeacherRank";
	}

	@Override
	protected Supplier<Boolean> usageCheck(Long id) {
		return () -> !teacherRepository.findByTeacherRankId(id).isEmpty();
	}
}
