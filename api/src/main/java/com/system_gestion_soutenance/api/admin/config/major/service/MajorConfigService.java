package com.system_gestion_soutenance.api.admin.config.major.service;

import com.system_gestion_soutenance.api.admin.config.major.dto.CreateMajorRequest;
import com.system_gestion_soutenance.api.admin.config.major.entity.Major;
import com.system_gestion_soutenance.api.admin.config.major.repository.MajorRepository;
import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class MajorConfigService {

	private final MajorRepository majorRepository;
	private final StudentRepository studentRepository;

	public MajorConfigService(MajorRepository majorRepository, StudentRepository studentRepository) {
		this.majorRepository = majorRepository;
		this.studentRepository = studentRepository;
	}

	public List<Major> findAll() {
		return majorRepository.findAll();
	}

	@Audited(action = "CREATE", entity = "Major")
	@Transactional
	public Major create(CreateMajorRequest request) {
		if (majorRepository.findByName(request.name()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Une filière avec ce nom existe déjà");
		}

		Major major = new Major();
		major.setName(request.name());
		return majorRepository.save(major);
	}

	@Audited(action = "UPDATE", entity = "Major")
	@Transactional
	public Major update(Long id, CreateMajorRequest request) {
		Major major = majorRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Filière non trouvée"));

		major.setName(request.name());
		return majorRepository.save(major);
	}

	@Audited(action = "DELETE", entity = "Major")
	@Transactional
	public void delete(Long id) {
		Major major = majorRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Filière non trouvée"));

		if (!studentRepository.findByMajorId(id).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Impossible de supprimer cette filière car des étudiants y sont inscrits");
		}

		majorRepository.delete(major);
	}
}
