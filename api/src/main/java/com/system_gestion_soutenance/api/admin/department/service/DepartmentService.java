package com.system_gestion_soutenance.api.admin.department.service;

import com.system_gestion_soutenance.api.admin.department.dto.CreateDepartmentRequest;
import com.system_gestion_soutenance.api.admin.department.entity.Department;
import com.system_gestion_soutenance.api.admin.department.repository.DepartmentRepository;
import com.system_gestion_soutenance.api.admin.faculty.entity.Faculty;
import com.system_gestion_soutenance.api.admin.faculty.repository.FacultyRepository;
import com.system_gestion_soutenance.api.admin.room.repository.RoomRepository;
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
public class DepartmentService {

	private final DepartmentRepository departmentRepository;
	private final TeacherRepository teacherRepository;
	private final RoomRepository roomRepository;
	private final FacultyRepository facultyRepository;

	public DepartmentService(DepartmentRepository departmentRepository, TeacherRepository teacherRepository,
			RoomRepository roomRepository, FacultyRepository facultyRepository) {
		this.departmentRepository = departmentRepository;
		this.teacherRepository = teacherRepository;
		this.roomRepository = roomRepository;
		this.facultyRepository = facultyRepository;
	}

	public List<Department> findAll() {
		return departmentRepository.findAll();
	}

	public Department findById(Long id) {
		return departmentRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Département non trouvé"));
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

		Faculty faculty = facultyRepository.findById(request.facultyId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faculté introuvable"));
		department.setFaculty(faculty);

		if (request.headId() != null) {
			Teacher head = teacherRepository.findById(request.headId()).orElseThrow(
					() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enseignant responsable introuvable"));
			department.setHead(head);
		}

		return departmentRepository.save(department);
	}

	@Audited(action = "UPDATE", entity = "Department")
	@Transactional
	public Department update(Long id, CreateDepartmentRequest request) {
		Department department = departmentRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Département non trouvé"));

		department.setName(request.name());
		department.setCode(request.code());

		Faculty faculty = facultyRepository.findById(request.facultyId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faculté introuvable"));
		department.setFaculty(faculty);

		if (request.headId() != null) {
			Teacher head = teacherRepository.findById(request.headId()).orElseThrow(
					() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enseignant responsable introuvable"));
			department.setHead(head);
		} else {
			department.setHead(null);
		}

		return departmentRepository.save(department);
	}

	@Audited(action = "DELETE", entity = "Department")
	@Transactional
	public void delete(Long id) {
		Department department = departmentRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Département non trouvé"));

		if (!teacherRepository.findByDepartmentId(id).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Impossible de supprimer ce département car des enseignants y sont rattachés");
		}

		if (!roomRepository.findByDepartment_Id(id).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Impossible de supprimer ce département car des salles y sont rattachées");
		}

		departmentRepository.delete(department);
	}
}
