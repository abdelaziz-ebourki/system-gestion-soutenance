package com.system_gestion_soutenance.api.user.service;

import com.system_gestion_soutenance.api.admin.department.repository.DepartmentRepository;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryMemberRepository;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserConstraintServiceTest {

	@Mock
	private DepartmentRepository departmentRepository;
	@Mock
	private JuryMemberRepository juryMemberRepository;
	@Mock
	private ProjectRepository projectRepository;

	@InjectMocks
	private UserConstraintService userConstraintService;

	@Test
	void checkTeacherDeletionConstraints_Success() {
		Long teacherId = 1L;
		when(departmentRepository.findByHead_Id(teacherId)).thenReturn(List.of());
		when(juryMemberRepository.findByTeacher_Id(teacherId)).thenReturn(List.of());
		when(projectRepository.findBySupervisorId(teacherId)).thenReturn(List.of());

		assertDoesNotThrow(() -> userConstraintService.checkTeacherDeletionConstraints(teacherId));
	}

	@Test
	void checkTeacherDeletionConstraints_IsDeptHead_ThrowsException() {
		Long teacherId = 1L;
		when(departmentRepository.findByHead_Id(teacherId))
				.thenReturn(List.of(new com.system_gestion_soutenance.api.admin.department.entity.Department()));

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> userConstraintService.checkTeacherDeletionConstraints(teacherId));
		assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
		assertTrue(ex.getReason().contains("responsable de département"));
	}

	@Test
	void checkTeacherDeletionConstraints_IsJuryMember_ThrowsException() {
		Long teacherId = 1L;
		when(departmentRepository.findByHead_Id(teacherId)).thenReturn(List.of());
		when(juryMemberRepository.findByTeacher_Id(teacherId))
				.thenReturn(List.of(new com.system_gestion_soutenance.api.coordinator.jury.entity.JuryMember()));

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> userConstraintService.checkTeacherDeletionConstraints(teacherId));
		assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
		assertTrue(ex.getReason().contains("membre d'un jury"));
	}

	@Test
	void checkTeacherDeletionConstraints_IsSupervisor_ThrowsException() {
		Long teacherId = 1L;
		when(departmentRepository.findByHead_Id(teacherId)).thenReturn(List.of());
		when(juryMemberRepository.findByTeacher_Id(teacherId)).thenReturn(List.of());
		when(projectRepository.findBySupervisorId(teacherId))
				.thenReturn(List.of(new com.system_gestion_soutenance.api.coordinator.project.entity.Project()));

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> userConstraintService.checkTeacherDeletionConstraints(teacherId));
		assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
		assertTrue(ex.getReason().contains("encadre des projets"));
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

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> userConstraintService.checkStudentDeletionConstraints(studentId));
		assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
		assertTrue(ex.getReason().contains("lié à des projets"));
	}
}
