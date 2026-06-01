package com.system_gestion_soutenance.api.user.service;

import com.system_gestion_soutenance.api.admin.department.repository.DepartmentRepository;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryMemberRepository;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserConstraintService {

	private final DepartmentRepository departmentRepository;
	private final JuryMemberRepository juryMemberRepository;
	private final ProjectRepository projectRepository;

	public UserConstraintService(DepartmentRepository departmentRepository, JuryMemberRepository juryMemberRepository,
			ProjectRepository projectRepository) {
		this.departmentRepository = departmentRepository;
		this.juryMemberRepository = juryMemberRepository;
		this.projectRepository = projectRepository;
	}

	public void checkTeacherDeletionConstraints(Long teacherId) {
		if (!departmentRepository.findByHead_Id(teacherId).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Impossible de supprimer cet enseignant car il est responsable de département(s)");
		}
		if (!juryMemberRepository.findByTeacher_Id(teacherId).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Impossible de supprimer cet enseignant car il est membre d'un jury");
		}
		if (!projectRepository.findBySupervisorId(teacherId).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Impossible de supprimer cet enseignant car il encadre des projets");
		}
	}

	public void checkStudentDeletionConstraints(Long studentId) {
		if (!projectRepository.findByStudentsId(studentId).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Impossible de supprimer cet étudiant car il est lié à des projets");
		}
	}
}
