package com.system_gestion_soutenance.api.admin.config.major.service;

import com.system_gestion_soutenance.api.admin.config.major.dto.CreateMajorRequest;
import com.system_gestion_soutenance.api.admin.config.major.entity.Major;
import com.system_gestion_soutenance.api.admin.config.major.repository.MajorRepository;
import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.common.service.BaseCrudService;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class MajorConfigService extends BaseCrudService<Major, Long, CreateMajorRequest> {

	private final MajorRepository majorRepository;
	private final StudentRepository studentRepository;

	public MajorConfigService(MajorRepository majorRepository, StudentRepository studentRepository) {
		super(majorRepository);
		this.majorRepository = majorRepository;
		this.studentRepository = studentRepository;
	}

	@Audited(action = "CREATE", entity = "Major")
	@Transactional
	public Major create(CreateMajorRequest request) {
		if (majorRepository.findByName(request.name()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Une filière avec ce nom existe déjà");
		}

		Major major = new Major();
		major.setName(request.name());
		return save(major);
	}

	@Audited(action = "UPDATE", entity = "Major")
	@Transactional
	public Major update(Long id, CreateMajorRequest request) {
		Major major = findByIdOrThrow(id, "Filière");
		major.setName(request.name());
		return save(major);
	}

	@Audited(action = "DELETE", entity = "Major")
	@Transactional
	public void delete(Long id) {
		deleteWithCheck(id, "Filière", () -> !studentRepository.findByMajorId(id).isEmpty());
	}
}
