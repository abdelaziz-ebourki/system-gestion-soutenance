package com.system_gestion_soutenance.api.user.service;

import com.system_gestion_soutenance.api.admin.config.grade.entity.Grade;
import com.system_gestion_soutenance.api.admin.config.grade.repository.GradeRepository;
import com.system_gestion_soutenance.api.admin.config.level.entity.Level;
import com.system_gestion_soutenance.api.admin.config.level.repository.LevelRepository;
import com.system_gestion_soutenance.api.admin.config.major.entity.Major;
import com.system_gestion_soutenance.api.admin.config.major.repository.MajorRepository;
import com.system_gestion_soutenance.api.admin.department.entity.Department;
import com.system_gestion_soutenance.api.admin.department.repository.DepartmentRepository;
import com.system_gestion_soutenance.api.user.dto.UpdateUserRequest;
import com.system_gestion_soutenance.api.user.entity.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class UserProfileService {

	private final MajorRepository majorRepository;
	private final LevelRepository levelRepository;
	private final GradeRepository gradeRepository;
	private final DepartmentRepository departmentRepository;

	public UserProfileService(MajorRepository majorRepository, LevelRepository levelRepository,
			GradeRepository gradeRepository, DepartmentRepository departmentRepository) {
		this.majorRepository = majorRepository;
		this.levelRepository = levelRepository;
		this.gradeRepository = gradeRepository;
		this.departmentRepository = departmentRepository;
	}

	public void updateBasicInfo(User user, UpdateUserRequest request) {
		if (request.lastName() != null)
			user.setLastName(request.lastName());
		if (request.firstName() != null)
			user.setFirstName(request.firstName());
	}

	public void updateStudentProfile(Student student, UpdateUserRequest request) {
		if (request.cne() != null)
			student.setCne(request.cne());
		if (request.majorId() != null) {
			Major major = majorRepository.findById(request.majorId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filière introuvable"));
			student.setMajor(major);
		}
		if (request.levelId() != null) {
			Level level = levelRepository.findById(request.levelId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Niveau introuvable"));
			student.setLevel(level);
		}
	}

	public void updateTeacherProfile(Teacher teacher, UpdateUserRequest request) {
		if (request.gradeId() != null) {
			Grade grade = gradeRepository.findById(request.gradeId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Grade introuvable"));
			teacher.setGrade(grade);
		}
		if (request.departmentId() != null) {
			Department dept = departmentRepository.findById(request.departmentId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Département introuvable"));
			teacher.setDepartment(dept);
		}
	}
}
