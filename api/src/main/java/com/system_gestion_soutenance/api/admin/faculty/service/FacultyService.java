package com.system_gestion_soutenance.api.admin.faculty.service;

import com.system_gestion_soutenance.api.admin.department.repository.DepartmentRepository;
import com.system_gestion_soutenance.api.admin.faculty.dto.CreateFacultyRequest;
import com.system_gestion_soutenance.api.admin.faculty.dto.UpdateFacultyRequest;
import com.system_gestion_soutenance.api.admin.faculty.entity.Faculty;
import com.system_gestion_soutenance.api.admin.faculty.repository.FacultyRepository;
import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import java.util.List;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import com.system_gestion_soutenance.api.common.exception.ResourceConflictException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@SuppressWarnings("PMD")

@Service
@Transactional(readOnly = true)
public class FacultyService {

	private final FacultyRepository facultyRepository;
	private final DepartmentRepository departmentRepository;
	private final TeacherRepository teacherRepository;

	public FacultyService(FacultyRepository facultyRepository, DepartmentRepository departmentRepository,
			TeacherRepository teacherRepository) {
		this.facultyRepository = facultyRepository;
		this.departmentRepository = departmentRepository;
		this.teacherRepository = teacherRepository;
	}

	public List<Faculty> findAll() {
		return facultyRepository.findAll();
	}

	public PaginatedResponse<Faculty> findAll(int page, int limit) {
		Page<Faculty> facultyPage = facultyRepository.findAll(PageRequest.of(page, limit));
		return new PaginatedResponse<>(facultyPage.getContent(), facultyPage.getTotalElements(),
				facultyPage.getTotalPages(), page, limit);
	}

	public Faculty findById(Long id) {
		return facultyRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Faculté non trouvée"));
	}

	@Audited(action = "CREATE", entity = "Faculty")
	@Transactional
	public Faculty create(CreateFacultyRequest request) {
		Faculty faculty = new Faculty();
		faculty.setName(request.name());
		faculty.setCode(request.code());
		faculty.setDean(resolveDean(request.deanId()));
		faculty.setLogoUrl(request.logoUrl());
		return facultyRepository.save(faculty);
	}

	@Audited(action = "UPDATE", entity = "Faculty")
	@Transactional
	public Faculty update(Long id, CreateFacultyRequest request) {
		Faculty faculty = facultyRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Faculté non trouvée"));

		faculty.setName(request.name());
		faculty.setCode(request.code());
		faculty.setDean(resolveDean(request.deanId()));
		faculty.setLogoUrl(request.logoUrl());
		return facultyRepository.save(faculty);
	}

	@Audited(action = "UPDATE", entity = "Faculty")
	@Transactional
	public Faculty updatePartial(Long id, UpdateFacultyRequest request) {
		Faculty faculty = facultyRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Faculté non trouvée"));

		if (request.name() != null) {
			faculty.setName(request.name());
		}
		if (request.code() != null) {
			faculty.setCode(request.code());
		}
		if (request.deanId() != null) {
			faculty.setDean(resolveDean(request.deanId()));
		}
		if (request.logoUrl() != null) {
			faculty.setLogoUrl(request.logoUrl());
		}
		return facultyRepository.save(faculty);
	}

	private Teacher resolveDean(Long deanId) {
		if (deanId == null)
			return null;
		return teacherRepository.findById(deanId)
				.orElseThrow(() -> new InvalidBusinessStateException("Enseignant (doyen) non trouvé"));
	}

	@Audited(action = "DELETE", entity = "Faculty")
	@Transactional
	public void delete(Long id) {
		Faculty faculty = facultyRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Faculté non trouvée"));

		if (!departmentRepository.findByFaculty_Id(id).isEmpty()) {
			throw new ResourceConflictException(
					"Impossible de supprimer cette faculté car des départements y sont rattachés");
		}

		facultyRepository.delete(faculty);
	}
}