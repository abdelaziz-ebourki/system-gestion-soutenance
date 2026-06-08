package com.system_gestion_soutenance.api.user.service;

import com.system_gestion_soutenance.api.admin.department.repository.DepartmentRepository;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.system_gestion_soutenance.api.common.exception.ResourceConflictException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserConstraintServiceTest {

	@Mock
	private DepartmentRepository departmentRepository;
	@Mock
	private DefenseRepository defenseRepository;
	@Mock
	private ProjectRepository projectRepository;

	@InjectMocks
	private UserConstraintService userConstraintService;

	@Test
	void checkTeacherDeletionConstraints_Success() {
		Long teacherId = 1L;
		when(departmentRepository.findByHead_Id(teacherId)).thenReturn(List.of());
		when(defenseRepository.existsByMembers_Teacher_Id(teacherId)).thenReturn(false);
		when(projectRepository.findBySupervisorId(teacherId)).thenReturn(List.of());

		assertDoesNotThrow(() -> userConstraintService.checkTeacherDeletionConstraints(teacherId));
	}

	@Test
	void checkTeacherDeletionConstraints_IsDeptHead_ThrowsException() {
		Long teacherId = 1L;
		when(departmentRepository.findByHead_Id(teacherId))
				.thenReturn(List.of(new com.system_gestion_soutenance.api.admin.department.entity.Department()));

		ResourceConflictException ex = assertThrows(ResourceConflictException.class,
				() -> userConstraintService.checkTeacherDeletionConstraints(teacherId));
		assertTrue(ex.getMessage().contains("responsable de département"));
	}

	@Test
	void checkTeacherDeletionConstraints_IsJuryMember_ThrowsException() {
		Long teacherId = 1L;

		when(departmentRepository.findByHead_Id(teacherId)).thenReturn(List.of());
		when(defenseRepository.existsByMembers_Teacher_Id(teacherId)).thenReturn(true);

		ResourceConflictException ex = assertThrows(ResourceConflictException.class,
				() -> userConstraintService.checkTeacherDeletionConstraints(teacherId));
		assertTrue(ex.getMessage().contains("membre d'un jury"));
	}

	@Test
	void checkTeacherDeletionConstraints_IsSupervisor_ThrowsException() {
		Long teacherId = 1L;
		when(departmentRepository.findByHead_Id(teacherId)).thenReturn(List.of());
		when(defenseRepository.existsByMembers_Teacher_Id(teacherId)).thenReturn(false);
		when(projectRepository.findBySupervisorId(teacherId))
				.thenReturn(List.of(new com.system_gestion_soutenance.api.coordinator.project.entity.Project()));

		ResourceConflictException ex = assertThrows(ResourceConflictException.class,
				() -> userConstraintService.checkTeacherDeletionConstraints(teacherId));
		assertTrue(ex.getMessage().contains("encadre des projets"));
	}

	@Test
	void checkStudentDeletionConstraints_Success() {
		Long studentId = 1L;
		when(projectRepository.findByStudentsId(studentId)).thenReturn(List.of());

		assertDoesNotThrow(() -> userConstraintService.checkStudentDeletionConstraints(studentId));
	}

	@Test
	void checkStudentDeletionConstraints_IsLinkedToProject_ThrowsException() {
		Long studentId = 1L;
		when(projectRepository.findByStudentsId(studentId))
				.thenReturn(List.of(new com.system_gestion_soutenance.api.coordinator.project.entity.Project()));

		ResourceConflictException ex = assertThrows(ResourceConflictException.class,
				() -> userConstraintService.checkStudentDeletionConstraints(studentId));
		assertTrue(ex.getMessage().contains("lié à des projets"));
	}
}
