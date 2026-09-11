package com.system_gestion_soutenance.api.admin.config.major.service;

import com.system_gestion_soutenance.api.admin.config.major.dto.CreateMajorRequest;
import com.system_gestion_soutenance.api.admin.config.major.dto.MajorDto;
import com.system_gestion_soutenance.api.admin.config.major.dto.UpdateMajorRequest;
import com.system_gestion_soutenance.api.admin.config.major.entity.Major;
import com.system_gestion_soutenance.api.admin.config.major.repository.MajorRepository;
import com.system_gestion_soutenance.api.admin.department.entity.Department;
import com.system_gestion_soutenance.api.admin.department.repository.DepartmentRepository;
import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.ConfigMapper;
import com.system_gestion_soutenance.api.common.service.AbstractNamedConfigService;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@SuppressWarnings("PMD")

@Service
@Transactional(readOnly = true)
public class MajorConfigService extends AbstractNamedConfigService<Major, CreateMajorRequest, UpdateMajorRequest> {

	private final MajorRepository majorRepository;
	private final StudentRepository studentRepository;
	private final DepartmentRepository departmentRepository;
	private final ConfigMapper configMapper;

	public MajorConfigService(MajorRepository majorRepository, StudentRepository studentRepository,
			DepartmentRepository departmentRepository, ConfigMapper configMapper) {
		super(majorRepository);
		this.majorRepository = majorRepository;
		this.studentRepository = studentRepository;
		this.departmentRepository = departmentRepository;
		this.configMapper = configMapper;
	}

	public PaginatedResponse<MajorDto> findAll(int page, int limit) {
		Page<Major> majorPage = majorRepository.findAll(PageRequest.of(page, limit));
		List<MajorDto> dtos = majorPage.getContent().stream().map(configMapper::toMajorDto)
				.collect(Collectors.toList());

		Map<Long, Long> counts = dtos.stream()
				.collect(Collectors.toMap(MajorDto::id, d -> studentRepository.countByMajorId(d.id())));

		List<MajorDto> enriched = dtos.stream().map(d -> new MajorDto(d.id(), d.name(), d.departmentId(),
				d.departmentName(), counts.getOrDefault(d.id(), 0L))).collect(Collectors.toList());

		return new PaginatedResponse<>(enriched, majorPage.getTotalElements(), majorPage.getTotalPages(), page, limit);
	}

	@Audited(action = "CREATE", entity = "Major")
	@Transactional
	public Major create(CreateMajorRequest request) {
		Major major = createNamed(request);
		applyDepartment(major, request.departmentId(), false);
		return save(major);
	}

	@Audited(action = "UPDATE", entity = "Major")
	@Transactional
	public Major update(Long id, CreateMajorRequest request) {
		Major major = updateNamed(id, request);
		applyDepartment(major, request.departmentId(), true);
		return save(major);
	}

	@Audited(action = "UPDATE", entity = "Major")
	@Transactional
	public Major updatePartial(Long id, UpdateMajorRequest request) {
		Major major = updatePartialNamed(id, request);
		if (request.departmentId() != null) {
			applyDepartment(major, request.departmentId(), false);
		}
		return save(major);
	}

	@Audited(action = "DELETE", entity = "Major")
	@Transactional
	public void delete(Long id) {
		deleteNamed(id);
	}

	@Override
	protected Major newEntity() {
		return new Major();
	}

	@Override
	protected void setName(Major entity, String name) {
		entity.setName(name);
	}

	@Override
	protected Optional<Major> findByName(String name) {
		return majorRepository.findByName(name);
	}

	@Override
	protected String duplicateMessage() {
		return "Une filière avec ce nom existe déjà";
	}

	@Override
	protected String entityLabel() {
		return "Filière";
	}

	@Override
	protected Supplier<Boolean> usageCheck(Long id) {
		return () -> !studentRepository.findByMajorId(id).isEmpty();
	}

	private void applyDepartment(Major major, Long departmentId, boolean clearWhenNull) {
		if (departmentId != null) {
			Department dept = departmentRepository.findById(departmentId)
					.orElseThrow(() -> new InvalidBusinessStateException("Département introuvable"));
			major.setDepartment(dept);
		} else if (clearWhenNull) {
			major.setDepartment(null);
		}
	}
}
