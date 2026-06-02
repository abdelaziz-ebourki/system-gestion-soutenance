package com.system_gestion_soutenance.api.admin.department.service;

import com.system_gestion_soutenance.api.admin.department.dto.CreateDepartmentRequest;
import com.system_gestion_soutenance.api.admin.department.entity.Department;
import com.system_gestion_soutenance.api.admin.department.repository.DepartmentRepository;
import com.system_gestion_soutenance.api.admin.faculty.entity.Faculty;
import com.system_gestion_soutenance.api.admin.faculty.repository.FacultyRepository;
import com.system_gestion_soutenance.api.admin.room.repository.RoomRepository;
import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.common.service.BaseCrudService;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class DepartmentService extends BaseCrudService<Department, Long, CreateDepartmentRequest> {

	private final DepartmentRepository departmentRepository;
	private final TeacherRepository teacherRepository;
	private final RoomRepository roomRepository;
	private final FacultyRepository facultyRepository;

	public DepartmentService(DepartmentRepository departmentRepository, TeacherRepository teacherRepository,
			RoomRepository roomRepository, FacultyRepository facultyRepository) {
		super(departmentRepository);
		this.departmentRepository = departmentRepository;
		this.teacherRepository = teacherRepository;
		this.roomRepository = roomRepository;
		this.facultyRepository = facultyRepository;
	}

	public Department findById(Long id) {
		return findByIdOrThrow(id, "Département");
	}

	@Audited(action = "CREATE", entity = "Department")
	@Transactional
	public Department create(CreateDepartmentRequest request) {
		if (departmentRepository.findByName(request.name()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un département avec ce nom existe déjà");
		}

		Department department = new Department();
		department.setName(request.name());
		department.setCode(request.code());

		if (request.facultyId() != null) {
			Faculty faculty = facultyRepository.findById(request.facultyId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faculté introuvable"));
			department.setFaculty(faculty);
		}

		if (request.headId() != null) {
			Teacher head = teacherRepository.findById(request.headId()).orElseThrow(
					() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enseignant responsable introuvable"));
			department.setHead(head);
		}

		return save(department);
	}

	@Audited(action = "UPDATE", entity = "Department")
	@Transactional
	public Department update(Long id, CreateDepartmentRequest request) {
		Department department = findByIdOrThrow(id, "Département");

		department.setName(request.name());
		department.setCode(request.code());

		if (request.facultyId() != null) {
			Faculty faculty = facultyRepository.findById(request.facultyId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faculté introuvable"));
			department.setFaculty(faculty);
		} else {
			department.setFaculty(null);
		}

		if (request.headId() != null) {
			Teacher head = teacherRepository.findById(request.headId()).orElseThrow(
					() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enseignant responsable introuvable"));
			department.setHead(head);
		} else {
			department.setHead(null);
		}

		return save(department);
	}

	@Audited(action = "DELETE", entity = "Department")
	@Transactional
	public void delete(Long id) {
		deleteWithCheck(id, "Département", () -> !teacherRepository.findByDepartmentId(id).isEmpty()
				|| !roomRepository.findByDepartment_Id(id).isEmpty());
	}
}
