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
import com.system_gestion_soutenance.api.common.service.BaseCrudService;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@SuppressWarnings("PMD")

@Service
@Transactional(readOnly = true)
public class MajorConfigService extends BaseCrudService<Major, Long, CreateMajorRequest> {

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
		if (majorRepository.findByName(request.name()).isPresent()) {
			throw new InvalidBusinessStateException("Une filière avec ce nom existe déjà");
		}

		Major major = new Major();
		major.setName(request.name());
		if (request.departmentId() != null) {
			Department dept = departmentRepository.findById(request.departmentId())
					.orElseThrow(() -> new InvalidBusinessStateException("Département introuvable"));
			major.setDepartment(dept);
		}
		return save(major);
	}

	@Audited(action = "UPDATE", entity = "Major")
	@Transactional
	public Major update(Long id, CreateMajorRequest request) {
		Major major = findByIdOrThrow(id, "Filière");
		major.setName(request.name());
		if (request.departmentId() != null) {
			Department dept = departmentRepository.findById(request.departmentId())
					.orElseThrow(() -> new InvalidBusinessStateException("Département introuvable"));
			major.setDepartment(dept);
		} else {
			major.setDepartment(null);
		}
		return save(major);
	}

	@Audited(action = "UPDATE", entity = "Major")
	@Transactional
	public Major updatePartial(Long id, UpdateMajorRequest request) {
		Major major = findByIdOrThrow(id, "Filière");
		if (request.name() != null) {
			major.setName(request.name());
		}
		if (request.departmentId() != null) {
			Department dept = departmentRepository.findById(request.departmentId())
					.orElseThrow(() -> new InvalidBusinessStateException("Département introuvable"));
			major.setDepartment(dept);
		}
		return save(major);
	}

	@Audited(action = "DELETE", entity = "Major")
	@Transactional
	public void delete(Long id) {
		deleteWithCheck(id, "Filière", () -> !studentRepository.findByMajorId(id).isEmpty());
	}
}