package com.system_gestion_soutenance.api.user.service;

import com.system_gestion_soutenance.api.admin.department.repository.DepartmentRepository;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.common.exception.ResourceConflictException;
import org.springframework.stereotype.Service;
@SuppressWarnings("PMD")

@Service
public class UserConstraintService {

	private final DepartmentRepository departmentRepository;
	private final DefenseRepository defenseRepository;
	private final ProjectRepository projectRepository;

	public UserConstraintService(DepartmentRepository departmentRepository, DefenseRepository defenseRepository,
			ProjectRepository projectRepository) {
		this.departmentRepository = departmentRepository;
		this.defenseRepository = defenseRepository;
		this.projectRepository = projectRepository;
	}

	public void checkTeacherDeletionConstraints(Long teacherId) {
		if (!departmentRepository.findByHead_Id(teacherId).isEmpty()) {
			throw new ResourceConflictException(
					"Impossible de supprimer cet enseignant car il est responsable de département(s)");
		}
		if (defenseRepository.existsByMembers_Teacher_Id(teacherId)) {
			throw new ResourceConflictException("Impossible de supprimer cet enseignant car il est membre d'un jury");
		}
		if (!projectRepository.findBySupervisorId(teacherId).isEmpty()) {
			throw new ResourceConflictException("Impossible de supprimer cet enseignant car il encadre des projets");
		}
	}

	public void checkStudentDeletionConstraints(Long studentId) {
		if (!projectRepository.findByStudentsId(studentId).isEmpty()) {
			throw new ResourceConflictException("Impossible de supprimer cet étudiant car il est lié à des projets");
		}
	}
}