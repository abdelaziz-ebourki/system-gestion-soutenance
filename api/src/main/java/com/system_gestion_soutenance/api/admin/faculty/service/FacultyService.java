package com.system_gestion_soutenance.api.admin.faculty.service;

import com.system_gestion_soutenance.api.admin.department.repository.DepartmentRepository;
import com.system_gestion_soutenance.api.admin.faculty.dto.CreateFacultyRequest;
import com.system_gestion_soutenance.api.admin.faculty.entity.Faculty;
import com.system_gestion_soutenance.api.admin.faculty.repository.FacultyRepository;
import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

	public Faculty findById(Long id) {
		return facultyRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculté non trouvée"));
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
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculté non trouvée"));

		faculty.setName(request.name());
		faculty.setCode(request.code());
		faculty.setDean(resolveDean(request.deanId()));
		faculty.setLogoUrl(request.logoUrl());
		return facultyRepository.save(faculty);
	}

	private Teacher resolveDean(Long deanId) {
		if (deanId == null) return null;
		return teacherRepository.findById(deanId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enseignant (doyen) non trouvé"));
	}

	@Audited(action = "DELETE", entity = "Faculty")
	@Transactional
	public void delete(Long id) {
		Faculty faculty = facultyRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculté non trouvée"));

		if (!departmentRepository.findByFaculty_Id(id).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Impossible de supprimer cette faculté car des départements y sont rattachés");
		}

		facultyRepository.delete(faculty);
	}
}
